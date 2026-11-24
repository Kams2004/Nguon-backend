package com.example.ngoun.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Properties;

/**
 * Sends through the primary account (Gmail, spring.mail.*) and switches to the
 * fallback SMTP provider (Mailgun, app.mail.fallback.*) shortly before Gmail's
 * daily cap, or as soon as Gmail refuses a message. Each route backs up the
 * other. Without fallback credentials it behaves exactly like a single sender.
 */
@Slf4j
@Service
public class EmailService {

    private static final Duration WINDOW = Duration.ofHours(24);
    private static final Duration QUOTA_PAUSE = Duration.ofHours(2);

    private record Route(String name, JavaMailSender sender, String from) {}

    private final Route primary;
    private final Route fallback; // null when not configured
    private final int primaryDailyLimit;
    private final Deque<Instant> primarySends = new ArrayDeque<>();
    private volatile Instant primaryPausedUntil = Instant.EPOCH;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String primaryAddress,
            @Value("${app.mail.from:}") String primaryFromOverride,
            @Value("${app.mail.sender-name:Nguon 2026}") String senderName,
            @Value("${app.mail.primary-daily-limit:400}") int primaryDailyLimit,
            @Value("${app.mail.fallback.host:smtp.mailgun.org}") String fallbackHost,
            @Value("${app.mail.fallback.port:587}") int fallbackPort,
            @Value("${app.mail.fallback.username:}") String fallbackUsername,
            @Value("${app.mail.fallback.password:}") String fallbackPassword,
            @Value("${app.mail.fallback.from:}") String fallbackFromOverride) {

        String primaryFrom = primaryFromOverride.isBlank() ? primaryAddress : primaryFromOverride;
        this.primary = new Route("gmail", mailSender, withName(senderName, primaryFrom));
        this.primaryDailyLimit = primaryDailyLimit;

        if (fallbackUsername.isBlank() || fallbackPassword.isBlank()) {
            this.fallback = null;
            log.info("Email: single route ({}), no fallback configured", primaryFrom);
        } else {
            JavaMailSenderImpl impl = new JavaMailSenderImpl();
            impl.setHost(fallbackHost);
            impl.setPort(fallbackPort);
            impl.setUsername(fallbackUsername);
            impl.setPassword(fallbackPassword);
            Properties p = impl.getJavaMailProperties();
            p.put("mail.smtp.auth", "true");
            p.put("mail.smtp.starttls.enable", "true");
            p.put("mail.smtp.starttls.required", "true");
            p.put("mail.smtp.connectiontimeout", "10000");
            p.put("mail.smtp.timeout", "15000");
            p.put("mail.smtp.writetimeout", "15000");
            String fallbackFrom = fallbackFromOverride.isBlank() ? fallbackUsername : fallbackFromOverride;
            this.fallback = new Route("mailgun", impl, withName(senderName, fallbackFrom));
            log.info("Email: {} first (up to {}/24h), then {} via {}", primaryFrom, primaryDailyLimit, fallbackFrom, fallbackHost);
        }
    }

    public void sendEmail(String to, String subject, String body) {
        boolean primaryFirst = primaryAvailable();
        List<Route> order = new ArrayList<>();
        if (primaryFirst || fallback == null) order.add(primary);
        if (fallback != null) order.add(fallback);
        if (!primaryFirst && fallback != null) order.add(primary); // last resort

        MailException last = null;
        for (Route route : order) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(route.from());
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                route.sender().send(message);
                if (route == primary) recordPrimarySend();
                if (route != order.get(0)) log.info("Email to {} sent via {} after {} failed", to, route.name(), order.get(0).name());
                return;
            } catch (MailException e) {
                last = e;
                if (route == primary && isQuotaExceeded(e)) {
                    primaryPausedUntil = Instant.now().plus(QUOTA_PAUSE);
                    log.warn("Gmail sending quota reached; using {} until {}", fallback != null ? fallback.name() : "nothing", primaryPausedUntil);
                } else {
                    log.warn("Email to {} via {} failed: {}", to, route.name(), firstLine(e));
                }
            }
        }
        throw last;
    }

    /** SMTP 550 5.4.5 is a provider's sending cap (e.g. Gmail's "Daily user sending limit exceeded"). */
    public static boolean isQuotaExceeded(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            String m = t.getMessage();
            if (m != null && (m.contains("5.4.5") || m.toLowerCase().contains("sending limit"))) return true;
        }
        return false;
    }

    private boolean primaryAvailable() {
        if (Instant.now().isBefore(primaryPausedUntil)) return false;
        synchronized (primarySends) {
            Instant cutoff = Instant.now().minus(WINDOW);
            while (!primarySends.isEmpty() && primarySends.peekFirst().isBefore(cutoff)) primarySends.pollFirst();
            return primarySends.size() < primaryDailyLimit;
        }
    }

    private void recordPrimarySend() {
        synchronized (primarySends) {
            primarySends.addLast(Instant.now());
        }
    }

    private static String withName(String name, String address) {
        return name == null || name.isBlank() ? address : name + " <" + address + ">";
    }

    private static String firstLine(Throwable e) {
        String m = String.valueOf(e.getMessage());
        int nl = m.indexOf('\n');
        return nl > 0 ? m.substring(0, nl) : m;
    }
}

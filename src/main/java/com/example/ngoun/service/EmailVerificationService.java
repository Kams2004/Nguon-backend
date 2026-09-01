package com.example.ngoun.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

/**
 * Best-effort check for whether an email address is plausibly deliverable —
 * this is NOT a guarantee, by design. Real-time mailbox verification is
 * unreliable for every mail provider (many accept every RCPT TO and only
 * bounce later, e.g. some Microsoft domains), and outbound port 25 is
 * blocked outright on plenty of hosts. So this only ever reports "does not
 * exist" on a *definitive* rejection straight from the recipient's own mail
 * server; anything inconclusive (no MX record at all, connection refused or
 * blocked, timeout, ambiguous response) is treated as "might exist" so a
 * real voter is never wrongly blocked by an environment limitation outside
 * our control.
 */
@Slf4j
@Service
public class EmailVerificationService {

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    public boolean mightExist(String email) {
        int at = email.indexOf('@');
        if (at < 0 || at == email.length() - 1) return false;
        String domain = email.substring(at + 1);

        List<String> mxHosts = resolveMx(domain);
        if (mxHosts.isEmpty()) {
            log.debug("No MX records for domain '{}' — cannot verify, assuming valid", domain);
            return true;
        }

        for (String host : mxHosts) {
            Boolean result = probeRcpt(host, email);
            if (result != null) return result;
        }
        return true; // every MX host was unreachable/inconclusive — fail open
    }

    private List<String> resolveMx(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("com.sun.jndi.dns.timeout.initial", "3000");
            InitialDirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
            Attribute mxAttr = attrs.get("MX");
            if (mxAttr == null) return List.of();

            List<String[]> parsed = new ArrayList<>();
            NamingEnumeration<?> it = mxAttr.getAll();
            while (it.hasMore()) {
                String[] parts = String.valueOf(it.next()).trim().split("\\s+");
                if (parts.length == 2) parsed.add(parts);
            }
            parsed.sort(Comparator.comparingInt(p -> Integer.parseInt(p[0])));

            List<String> hosts = new ArrayList<>();
            for (String[] p : parsed) {
                String host = p[1].endsWith(".") ? p[1].substring(0, p[1].length() - 1) : p[1];
                hosts.add(host);
            }
            return hosts;
        } catch (NamingException | NumberFormatException e) {
            log.debug("MX lookup failed for '{}': {}", domain, e.getMessage());
            return List.of();
        }
    }

    /** @return true = accepted, false = definitively rejected, null = inconclusive */
    private Boolean probeRcpt(String host, String email) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, 25), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(READ_TIMEOUT_MS);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            if (!readExpect(in, "220")) return null;
            send(out, "EHLO nguonevents.com");
            if (!readExpect(in, "250")) return null;
            send(out, "MAIL FROM:<verify@nguonevents.com>");
            if (!readExpect(in, "250")) return null;
            send(out, "RCPT TO:<" + email + ">");
            String response = readLine(in);
            send(out, "QUIT");

            if (response == null) return null;
            if (response.startsWith("250")) return true;
            if (response.startsWith("550") || response.startsWith("551") || response.startsWith("553")) return false;
            return null; // greylisted, temp failure, or ambiguous — inconclusive
        } catch (IOException e) {
            log.debug("SMTP probe to {} failed/blocked: {}", host, e.getMessage());
            return null;
        }
    }

    private void send(PrintWriter out, String line) {
        out.print(line + "\r\n");
        out.flush();
    }

    private boolean readExpect(BufferedReader in, String code) throws IOException {
        String line = readLine(in);
        return line != null && line.startsWith(code);
    }

    // Multi-line SMTP responses use "250-" continuation lines, the final line uses "250 ".
    private String readLine(BufferedReader in) throws IOException {
        String line;
        String last = null;
        while ((line = in.readLine()) != null) {
            last = line;
            if (line.length() < 4 || line.charAt(3) != '-') break;
        }
        return last;
    }
}

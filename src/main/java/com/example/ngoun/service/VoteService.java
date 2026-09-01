package com.example.ngoun.service;

import com.example.ngoun.dto.VoteConfirmResponse;
import com.example.ngoun.dto.VoteOtpResponse;
import com.example.ngoun.dto.VoterDto;
import com.example.ngoun.model.Vote;
import com.example.ngoun.model.VoteProfile;
import com.example.ngoun.repository.VoteProfileRepository;
import com.example.ngoun.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteService {

    private static final int OTP_TTL_MINUTES = 10;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final VoteRepository voteRepository;
    private final VoteProfileRepository voteProfileRepository;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public VoteOtpResponse requestOtp(Long voteProfileId, String rawEmail) {
        String email = normalize(rawEmail);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return VoteOtpResponse.fail("Adresse email invalide.");
        }

        Optional<VoteProfile> profileOpt = voteProfileRepository.findById(voteProfileId);
        if (profileOpt.isEmpty()) {
            return VoteOtpResponse.fail("Profil introuvable.");
        }

        Optional<Vote> existing = voteRepository.findByEmail(email);
        if (existing.isPresent() && existing.get().isVerified()) {
            return VoteOtpResponse.fail("Cette adresse email a déjà voté.");
        }

        if (!emailVerificationService.mightExist(email)) {
            return VoteOtpResponse.fail("Cette adresse email n'existe pas.");
        }

        String otp = generateOtp();
        Vote vote = existing.orElseGet(Vote::new);
        vote.setEmail(email);
        vote.setVoteProfile(profileOpt.get());
        vote.setOtpCode(otp);
        vote.setOtpExpiresAt(LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES));
        vote.setVerified(false);
        voteRepository.save(vote);

        try {
            emailService.sendEmail(
                    email,
                    "Votre code de vote — Nguon 2026",
                    "Votre code de confirmation est : " + otp + "\n\n"
                            + "Ce code expire dans " + OTP_TTL_MINUTES + " minutes.\n\n"
                            + "Si vous n'avez pas demandé ce code, vous pouvez ignorer cet email."
            );
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}", email, e);
            return VoteOtpResponse.fail("Impossible d'envoyer le code. Veuillez réessayer.");
        }

        return VoteOtpResponse.ok();
    }

    @Transactional
    public VoteConfirmResponse confirmOtp(String rawEmail, String rawOtp) {
        String email = normalize(rawEmail);
        String otp = rawOtp == null ? "" : rawOtp.trim();

        Optional<Vote> voteOpt = voteRepository.findByEmail(email);
        if (voteOpt.isEmpty()) {
            return VoteConfirmResponse.fail("Aucune demande de vote trouvée pour cet email.");
        }

        Vote vote = voteOpt.get();
        if (vote.isVerified()) {
            return VoteConfirmResponse.fail("Cette adresse email a déjà voté.");
        }
        if (vote.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            return VoteConfirmResponse.fail("Code expiré, veuillez redemander un code.");
        }
        if (!vote.getOtpCode().equalsIgnoreCase(otp)) {
            return VoteConfirmResponse.fail("Code invalide.");
        }

        vote.setVerified(true);
        vote.setVerifiedAt(LocalDateTime.now());
        voteRepository.save(vote);

        VoteProfile profile = vote.getVoteProfile();
        profile.setVoteCount(profile.getVoteCount() + 1);
        voteProfileRepository.save(profile);

        return VoteConfirmResponse.ok(profile.getName());
    }

    public List<VoterDto> votersOf(Long voteProfileId) {
        return voteRepository.findByVoteProfileIdAndVerifiedTrueOrderByVerifiedAtDesc(voteProfileId)
                .stream()
                .map(v -> new VoterDto(v.getEmail(), v.getVerifiedAt()))
                .toList();
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    // Excludes visually ambiguous characters (0/O, 1/I/L) since this is
    // hand-typed from an email into a confirmation box.
    private static final String OTP_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int OTP_LENGTH = 6;

    private String generateOtp() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) sb.append(OTP_CHARS.charAt(random.nextInt(OTP_CHARS.length())));
        return sb.toString();
    }
}

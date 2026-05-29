package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.Entitie.EmailVerificationToken;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.EmailVerificationTokenRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.exception.EmailVerificationException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final IEmailService emailService;
    private final ApprovalSettingsService approvalSettingsService;

    @Value("${app.email-verification.expiration-hours:24}")
    private int expirationHours;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /** Comptes déjà actifs avant l'ajout de la vérification email. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateExistingVerifiedUsers() {
        int backfilled = userRepository.backfillNullEmailVerified();
        if (backfilled > 0) {
            log.info("Migration: {} compte(s) avec email_verified NULL → false", backfilled);
        }

        userRepository.findAll().forEach(user -> {
            if (user.getRole() == Role.ADMIN) {
                if (!user.isEmailVerified()) {
                    user.setEmailVerified(true);
                    userRepository.save(user);
                }
                return;
            }
            if (user.isEnabled() && !user.isEmailVerified()) {
                user.setEmailVerified(true);
                userRepository.save(user);
                log.info("Migration: email marqué vérifié pour compte actif {}", user.getEmail());
            }
        });
    }

    @Transactional
    public String sendVerificationEmail(User user) {
        if (user.getRole() == Role.ADMIN) {
            return null;
        }
        if (user.isEmailVerified()) {
            return null;
        }

        tokenRepository.invalidateActiveTokensForUser(user);

        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(expirationHours))
                .build();
        tokenRepository.save(token);

        emailService.sendEmailVerification(user, tokenValue);
        log.info("Email de vérification envoyé à {}", user.getEmail());
        return frontendUrl + "/verify-email?token=" + tokenValue;
    }

    @Transactional
    public void verifyEmail(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new EmailVerificationException("INVALID_TOKEN", "Lien invalide.");
        }

        EmailVerificationToken token = tokenRepository.findByTokenAndUsedAtIsNull(tokenValue.trim())
                .orElseThrow(() -> new EmailVerificationException("INVALID_TOKEN", "Lien invalide."));

        if (token.isExpired()) {
            throw new EmailVerificationException("EXPIRED_TOKEN", "Lien expiré.");
        }

        User user = token.getUser();
        if (user.getRole() == Role.ADMIN) {
            throw new EmailVerificationException("INVALID_TOKEN", "Lien invalide.");
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        if (approvalSettingsService.shouldAutoApprove(user.getEmail())) {
            user.setEnabled(true);
            userRepository.save(user);
            log.info("Compte auto-approuvé après vérification email: {}", user.getEmail());
        }

        log.info("Email vérifié pour {}", user.getEmail());
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte associé à cet email."));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Aucun compte associé à cet email.");
        }
        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Cet email est déjà vérifié.");
        }

        sendVerificationEmail(user);
    }

    public boolean isEmailVerificationRequired() {
        return approvalSettingsService.getSettings().isRequireEmailVerification();
    }
}

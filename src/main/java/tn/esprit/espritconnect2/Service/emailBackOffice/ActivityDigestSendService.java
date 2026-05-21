package tn.esprit.espritconnect2.Service.emailBackOffice;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.DigestConfig;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.DigestConfigRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityDigestSendService {

    private final DigestConfigRepository configRepo;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final ActivityDigestComposerService composer;

    @Transactional
    public void sendNowToAllEnabledUsers() {
        DigestConfig cfg = configRepo.findById(1L).orElseThrow();
        if (!Boolean.TRUE.equals(cfg.getActif())) {
            throw new IllegalStateException("Digest désactivé");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = cfg.getLastSentAt() != null ? cfg.getLastSentAt() : now.minusDays(cfg.getFrequence().equals("DAILY") ? 1 : 7);

        List<User> recipients = userRepository.findAll().stream()
                .filter(User::isEnabled)
                .toList();

        for (User u : recipients) {
            try {
                String html = composer.buildHtml(cfg, start, now, u.getNom());
                sendEmail(u.getEmail(), cfg.getSujet(), html);
            } catch (Exception e) {
                log.error("Erreur envoi digest à {}: {}", u.getEmail(), e.getMessage());
            }
        }

        cfg.setLastSentAt(now);
        configRepo.save(cfg);
    }

    public String buildPreviewHtml() {
        DigestConfig cfg = configRepo.findById(1L).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(cfg.getFrequence().equals("DAILY") ? 1 : 7);
        return composer.buildHtml(cfg, start, now, "Utilisateur Demo");
    }

    private void sendEmail(String to, String subject, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject != null && !subject.isBlank() ? subject : "What's new on Esprit");
        helper.setFrom("noreply@esprit.tn");
        helper.setText(html, true);
        mailSender.send(message);
    }
}
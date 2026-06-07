package tn.esprit.espritconnect2.Service.emailBackOffice;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.DigestConfig;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.MailingListMember;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.DigestConfigRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.MailingListMemberRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityDigestSendService {

    private final DigestConfigRepository configRepo;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final ActivityDigestComposerService composer;
    private final MailingListMemberRepository memberRepo;

    @Value("${app.upload.root}")
    private String uploadRoot;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Transactional
    public void sendNowToAllEnabledUsers() {
        DigestConfig cfg = loadConfigOrThrow();
        if (!Boolean.TRUE.equals(cfg.getActif())) {
            throw new IllegalStateException("Digest désactivé");
        }
        if (cfg.getMailingListId() == null) {
            throw new IllegalStateException("Aucune liste de diffusion sélectionnée pour ce digest.");
        }

        LocalDateTime now = LocalDateTime.now();
        // Utilise toujours la fenêtre complète définie par la fréquence (1 jour pour DAILY, 7 jours pour WEEKLY)
        // afin que le digest contienne toujours toutes les actualités de la période, même en cas d'envois manuels répétés
        LocalDateTime start = now.minusDays(daysForFrequency(cfg.getFrequence()));

        List<MailingListMember> recipients = memberRepo.findByMailingList_Id(cfg.getMailingListId());
        if (recipients.isEmpty()) {
            throw new IllegalStateException("La liste de diffusion sélectionnée est vide ou n'existe pas.");
        }

        // Resolving the banner file locally if it is configured
        Path bannerPath = null;
        boolean isTempBanner = false;
        if (cfg.getBannerUrl() != null && !cfg.getBannerUrl().isBlank()) {
            try {
                String bannerUrl = cfg.getBannerUrl();
                if (bannerUrl.startsWith("data:image/")) {
                    int base64Comma = bannerUrl.indexOf(",");
                    if (base64Comma >= 0) {
                        String base64Data = bannerUrl.substring(base64Comma + 1);
                        byte[] decoded = java.util.Base64.getDecoder().decode(base64Data.trim());
                        
                        // Extract extension
                        String ext = ".png";
                        int colon = bannerUrl.indexOf(":");
                        int semicolon = bannerUrl.indexOf(";");
                        if (colon >= 0 && semicolon > colon) {
                            String mimeType = bannerUrl.substring(colon + 1, semicolon);
                            if (mimeType.contains("jpeg") || mimeType.contains("jpg")) {
                                ext = ".jpg";
                            } else if (mimeType.contains("webp")) {
                                ext = ".webp";
                            } else if (mimeType.contains("gif")) {
                                ext = ".gif";
                            }
                        }
                        
                        Path tempBanner = Files.createTempFile("digest-banner-temp-", ext);
                        Files.write(tempBanner, decoded);
                        bannerPath = tempBanner;
                        isTempBanner = true;
                        log.info("Bannière Base64 décodée et écrite dans le fichier temporaire : {}", bannerPath);
                    }
                } else {
                    String filename = bannerUrl.substring(bannerUrl.lastIndexOf('/') + 1);
                    bannerPath = Paths.get(uploadRoot).resolve("banners").resolve(filename);
                    if (!Files.exists(bannerPath)) {
                        log.warn("Fichier de bannière introuvable localement à l'emplacement : {}", bannerPath);
                        bannerPath = null;
                    }
                }
            } catch (Exception e) {
                log.error("Erreur lors de la résolution de la bannière : {}", e.getMessage(), e);
                bannerPath = null;
            }
        }

        int successCount = 0;
        Exception lastException = null;

        for (MailingListMember m : recipients) {
            try {
                String html = composer.buildHtml(cfg, start, now, m.getNom() != null ? m.getNom() : "Utilisateur", true);
                sendEmail(m.getEmail(), cfg.getSujet(), html, bannerPath);
                successCount++;
            } catch (Exception e) {
                log.error("Erreur envoi digest à {}: {}", m.getEmail(), e.getMessage());
                lastException = e;
            }
        }

        if (successCount == 0) {
            if (isTempBanner && bannerPath != null) {
                try {
                    Files.deleteIfExists(bannerPath);
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Échec d'envoi du digest. Veuillez vérifier votre configuration de serveur SMTP. Erreur: " 
                    + (lastException != null ? lastException.getMessage() : "Inconnue"));
        }

        if (isTempBanner && bannerPath != null) {
            try {
                Files.deleteIfExists(bannerPath);
                log.info("Fichier de bannière temporaire supprimé : {}", bannerPath);
            } catch (Exception e) {
                log.warn("Impossible de supprimer la bannière temporaire : {}", e.getMessage());
            }
        }

        cfg.setLastSentAt(now);
        configRepo.save(cfg);
    }

    public String buildPreviewHtml() {
        DigestConfig cfg = loadConfigOrThrow();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(daysForFrequency(cfg.getFrequence()));
        try {
            return composer.buildHtml(cfg, start, now, "Utilisateur Demo", false);
        } catch (Exception ex) {
            log.error("Erreur lors de la génération complète de l'aperçu digest: {}", ex.getMessage(), ex);
            return buildSafeFallbackPreview(cfg, now);
        }
    }

    /**
     * Historically, digest config was expected at ID=1.
     * In some environments, auto-increment can assign a different ID,
     * so we gracefully fall back to the first available row.
     */
    private DigestConfig loadConfigOrThrow() {
        Optional<DigestConfig> cfg = configRepo.findById(1L)
                .or(() -> configRepo.findAll().stream().findFirst());

        return cfg.orElseThrow(() -> new IllegalStateException(
                "Aucune configuration Digest trouvée. Veuillez sauvegarder la configuration depuis l'interface admin."
        ));
    }

    private long daysForFrequency(String frequency) {
        return "DAILY".equalsIgnoreCase(frequency) ? 1L : 7L;
    }

    private String buildSafeFallbackPreview(DigestConfig cfg, LocalDateTime now) {
        String subject = (cfg.getSujet() != null && !cfg.getSujet().isBlank())
                ? cfg.getSujet()
                : "What's new on Esprit";

        String banner = (cfg.getBannerUrl() != null && !cfg.getBannerUrl().isBlank())
                ? "<img src=\"" + cfg.getBannerUrl() + "\" alt=\"Digest banner\" style=\"display:block;width:100%;max-width:600px;height:auto;border:0;\"/>"
                : "<div style=\"background:#dc2626;color:#fff;padding:24px;text-align:center;font:700 28px Arial,sans-serif;\">ESPRIT Connect</div>";

        String adminHtml = cfg.getTemplateHtml() == null ? "" : cfg.getTemplateHtml();

        String htmlTemplate = """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>Activity Digest Preview</title>
            </head>
            <body style="margin:0;padding:16px;background:#f3f4f6;font-family:Arial,sans-serif;">
              <div style="max-width:600px;margin:0 auto;background:#fff;border:1px solid #e5e7eb;border-radius:10px;overflow:hidden;">
                %s
                <div style="padding:24px;">
                  <h2 style="margin:0 0 12px;color:#111827;">%s</h2>
                  <p style="margin:0 0 16px;color:#4b5563;">Aperçu généré en mode sécurisé le %s.</p>
                  <div style="color:#374151;line-height:1.6;">%s</div>
                  <div style="margin-top:16px;padding:12px;border-radius:8px;background:#fff7ed;color:#9a3412;font-size:13px;">
                    Certaines sections automatiques n'ont pas pu être chargées. Vérifiez les logs backend.
                  </div>
                </div>
              </div>
            </body>
            </html>
        """;

        return htmlTemplate
                .replace("%s", "%%s")
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(banner))
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(escapeHtml(subject)))
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(now.toString()))
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(adminHtml));
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void sendEmail(String to, String subject, String html, Path bannerPath) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        
        // MimeMessageHelper with MULTIPART_MODE_RELATED is required to attach inline resources (CID)
        // so that they display correctly in mobile mail clients like Gmail App.
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_RELATED, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject != null && !subject.isBlank() ? subject : "What's new on Esprit");
        
        // Setting sender email with a beautiful display name 'Esprit Connect'
        helper.setFrom(senderEmail, "Esprit Connect");
        
        helper.setText(html, true);

        // Attaching the banner image in the email content as 'bannerImage' (matches src="cid:bannerImage" in HTML)
        if (bannerPath != null && Files.exists(bannerPath)) {
            String contentType = Files.probeContentType(bannerPath);
            if (contentType == null) {
                String filename = bannerPath.getFileName().toString().toLowerCase();
                if (filename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (filename.endsWith(".webp")) {
                    contentType = "image/webp";
                } else {
                    contentType = "image/png";
                }
            }
            helper.addInline("bannerImage", new org.springframework.core.io.FileSystemResource(bannerPath.toFile()), contentType);
        }

        mailSender.send(message);
    }
}
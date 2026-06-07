package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Petit service technique : envoyer un email HTML via JavaMailSender.
 * Réutilisé par les campagnes + anniversaire + tests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailDispatchService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String smtpUsername;

    @Value("${app.mail.from:}")
    private String configuredFrom;

    public void sendHtml(String to, String from, String subject, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        // Gmail/SMTP providers often enforce the authenticated address as "From".
        // So we always use the SMTP username, but with a friendly display name.
        String effectiveFrom = (smtpUsername != null && !smtpUsername.isBlank())
                ? smtpUsername
                : ((configuredFrom != null && !configuredFrom.isBlank()) ? configuredFrom : from);
        helper.setFrom(effectiveFrom, "Esprit Connect");

        // If caller provided a different "from", keep it as Reply-To (optional).
        if (from != null && !from.isBlank() && !from.equalsIgnoreCase(effectiveFrom)) {
            helper.setReplyTo(from);
        }
        helper.setSubject(subject);
        helper.setText(html, true);
        try {
            mailSender.send(message);
            log.info("Email envoyé à {} (from={})", to, effectiveFrom);
        } catch (Exception ex) {
            log.error("Échec envoi email à {} (from={}): {}", to, effectiveFrom, ex.getMessage(), ex);
            throw ex;
        }
    }
}
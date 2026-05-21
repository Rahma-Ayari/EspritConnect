package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Petit service technique : envoyer un email HTML via JavaMailSender.
 * Réutilisé par les campagnes + anniversaire + tests.
 */
@Service
@RequiredArgsConstructor
public class EmailDispatchService {

    private final JavaMailSender mailSender;

    public void sendHtml(String to, String from, String subject, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setFrom(from);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }
}
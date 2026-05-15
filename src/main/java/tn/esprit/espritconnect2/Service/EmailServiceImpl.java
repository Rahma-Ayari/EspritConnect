package tn.esprit.espritconnect2.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.AdministrateurRepository;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AdministrateurRepository administrateurRepository;
    private final ApprovalSettingsService approvalSettingsService;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.mail.admin-notification.subject}")
    private String adminNotificationSubject;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Async
    public void sendNewRegistrationNotification(User user) {
        if (!approvalSettingsService.getSettings().isEmailNotificationsOnNewRegistration()) {
            log.debug("Email notifications disabled, skipping notification for user: {}", user.getEmail());
            return;
        }

        List<String> adminEmails = administrateurRepository.findAllEmails();

        if (adminEmails.isEmpty()) {
            log.warn("No admin emails found, cannot send registration notification for user: {}", user.getEmail());
            return;
        }

        Context context = new Context(Locale.FRENCH);
        context.setVariable("userName", user.getNom());
        context.setVariable("userEmail", user.getEmail());
        context.setVariable("userRole", formatRole(user.getRole().name()));
        context.setVariable("registrationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        context.setVariable("approvalUrl", frontendUrl + "/admin/user-approval");
        context.setVariable("settingsUrl", frontendUrl + "/admin/user-approval?tab=settings");

        String htmlContent = templateEngine.process("email/new-registration-notification", context);

        for (String adminEmail : adminEmails) {
            try {
                sendHtmlEmail(adminEmail, adminNotificationSubject, htmlContent);
                log.info("Registration notification sent to admin: {} for new user: {}", adminEmail, user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send registration notification to admin: {} for user: {}", adminEmail, user.getEmail(), e);
            }
        }
    }

    @Override
    @Async
    public void sendApprovalNotification(User user) {
        if (!approvalSettingsService.getSettings().isNotifyUserOnApproval()) {
            log.debug("User approval notifications disabled, skipping for user: {}", user.getEmail());
            return;
        }

        Context context = new Context(Locale.FRENCH);
        context.setVariable("userName", user.getNom());
        context.setVariable("loginUrl", frontendUrl + "/login");

        String htmlContent = templateEngine.process("email/user-approved", context);

        try {
            sendHtmlEmail(user.getEmail(), "Votre compte EspritConnect a été approuvé", htmlContent);
            log.info("Approval notification sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send approval notification to user: {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void sendDeclineNotification(User user) {
        if (!approvalSettingsService.getSettings().isNotifyUserOnDecline()) {
            log.debug("User decline notifications disabled, skipping for user: {}", user.getEmail());
            return;
        }

        Context context = new Context(Locale.FRENCH);
        context.setVariable("userName", user.getNom());
        context.setVariable("contactEmail", "support@esprit.tn");

        String htmlContent = templateEngine.process("email/user-declined", context);

        try {
            sendHtmlEmail(user.getEmail(), "Votre demande d'inscription EspritConnect", htmlContent);
            log.info("Decline notification sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send decline notification to user: {}", user.getEmail(), e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String formatRole(String role) {
        return switch (role) {
            case "ETUDIANT" -> "Étudiant";
            case "ALUMNI" -> "Alumni";
            case "ENTREPRISE" -> "Entreprise";
            case "ENSEIGNANT" -> "Enseignant";
            default -> role;
        };
    }
}

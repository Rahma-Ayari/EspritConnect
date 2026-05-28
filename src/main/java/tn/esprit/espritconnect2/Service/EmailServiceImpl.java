package tn.esprit.espritconnect2.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.AdministrateurRepository;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AdministrateurRepository administrateurRepository;
    private final ApprovalSettingsService approvalSettingsService;
    
    private SmartMailingService smartMailingService;
    
    @Autowired
    public void setSmartMailingService(@Lazy SmartMailingService smartMailingService) {
        this.smartMailingService = smartMailingService;
    }

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
        // Déléguer au Smart Mailing Service pour une gestion intelligente des notifications
        smartMailingService.queueNewRegistrationNotification(user);
        log.debug("New registration notification queued for smart mailing: {}", user.getEmail());
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

    @Override
    @Async
    public void sendWelcomeEmailWithTemporaryPassword(User user, String temporaryPassword) {
        Context context = new Context(Locale.FRENCH);
        context.setVariable("userName", user.getNom());
        context.setVariable("userEmail", user.getEmail());
        context.setVariable("temporaryPassword", temporaryPassword);
        context.setVariable("loginUrl", frontendUrl + "/login");
        context.setVariable("userRole", formatRole(user.getRole().name()));

        String htmlContent = buildWelcomeEmailContent(context);

        try {
            sendHtmlEmail(user.getEmail(), "Bienvenue sur EspritConnect - Vos identifiants de connexion", htmlContent);
            log.info("Welcome email with temporary password sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to user: {}", user.getEmail(), e);
        }
    }

    private String buildWelcomeEmailContent(Context context) {
        try {
            return templateEngine.process("email/user-welcome-with-password", context);
        } catch (Exception e) {
            log.warn("Template 'user-welcome-with-password' not found, using inline HTML");
            String userName = (String) context.getVariable("userName");
            String userEmail = (String) context.getVariable("userEmail");
            String temporaryPassword = (String) context.getVariable("temporaryPassword");
            String loginUrl = (String) context.getVariable("loginUrl");
            String userRole = (String) context.getVariable("userRole");
            
            return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #dc2626, #991b1b); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                        .credentials { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc2626; }
                        .credentials p { margin: 8px 0; }
                        .password { font-family: monospace; font-size: 18px; color: #dc2626; font-weight: bold; }
                        .btn { display: inline-block; background: #dc2626; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin-top: 20px; }
                        .warning { background: #fef3c7; padding: 15px; border-radius: 8px; margin-top: 20px; border-left: 4px solid #f59e0b; }
                        .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Bienvenue sur EspritConnect!</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            <p>Un compte a été créé pour vous sur la plateforme EspritConnect en tant que <strong>%s</strong>.</p>
                            
                            <div class="credentials">
                                <p><strong>Vos identifiants de connexion:</strong></p>
                                <p>Email: <strong>%s</strong></p>
                                <p>Mot de passe temporaire: <span class="password">%s</span></p>
                            </div>
                            
                            <div class="warning">
                                <strong>Important:</strong> Pour des raisons de sécurité, veuillez changer votre mot de passe lors de votre première connexion.
                            </div>
                            
                            <p style="text-align: center;">
                                <a href="%s" class="btn">Se connecter</a>
                            </p>
                        </div>
                        <div class="footer">
                            <p>© 2024 EspritConnect - Tous droits réservés</p>
                            <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, userRole, userEmail, temporaryPassword, loginUrl);
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

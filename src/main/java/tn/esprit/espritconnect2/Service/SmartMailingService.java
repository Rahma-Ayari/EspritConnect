package tn.esprit.espritconnect2.Service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.SmartMailingSettingsDTO;
import tn.esprit.espritconnect2.Entitie.PendingEmailNotification;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.SmartMailingSettings;
import tn.esprit.espritconnect2.Entitie.SmartMailingSettings.NotificationMode;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.AdministrateurRepository;
import tn.esprit.espritconnect2.Repository.PendingEmailNotificationRepository;
import tn.esprit.espritconnect2.Repository.SmartMailingSettingsRepository;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartMailingService {

    private final SmartMailingSettingsRepository settingsRepository;
    private final PendingEmailNotificationRepository notificationRepository;
    private final AdministrateurRepository administrateurRepository;
    private final JavaMailSender mailSender;
    private final ApprovalSettingsService approvalSettingsService;

    private final AtomicReference<SmartMailingSettingsDTO> currentSettings = new AtomicReference<>();

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        SmartMailingSettings settings = settingsRepository.findById(1L).orElseGet(() -> {
            SmartMailingSettings defaultSettings = SmartMailingSettings.builder()
                    .id(1L)
                    .enabled(true)
                    .batchThreshold(5)
                    .batchWindowMinutes(30)
                    .notificationMode(NotificationMode.SMART)
                    .dailyDigestHour(8)
                    .prioritizeEnterprise(true)
                    .prioritizeAlumni(false)
                    .smartThresholdPerHour(10)
                    .dashboardNotificationsEnabled(true)
                    .build();
            return settingsRepository.save(defaultSettings);
        });
        currentSettings.set(mapToDTO(settings));
        log.info("Smart Mailing settings loaded: mode={}, threshold={}", 
                settings.getNotificationMode(), settings.getBatchThreshold());
    }

    public SmartMailingSettingsDTO getSettings() {
        return currentSettings.get();
    }

    @Transactional
    public SmartMailingSettingsDTO updateSettings(SmartMailingSettingsDTO dto) {
        SmartMailingSettings settings = SmartMailingSettings.builder()
                .id(1L)
                .enabled(dto.isEnabled())
                .batchThreshold(dto.getBatchThreshold())
                .batchWindowMinutes(dto.getBatchWindowMinutes())
                .notificationMode(dto.getNotificationMode())
                .dailyDigestHour(dto.getDailyDigestHour())
                .prioritizeEnterprise(dto.isPrioritizeEnterprise())
                .prioritizeAlumni(dto.isPrioritizeAlumni())
                .smartThresholdPerHour(dto.getSmartThresholdPerHour())
                .dashboardNotificationsEnabled(dto.isDashboardNotificationsEnabled())
                .build();

        settingsRepository.save(settings);
        currentSettings.set(dto);

        log.info("Smart Mailing settings updated: mode={}, threshold={}", 
                dto.getNotificationMode(), dto.getBatchThreshold());
        return dto;
    }

    @Async
    @Transactional
    public void queueNewRegistrationNotification(User user) {
        if (!approvalSettingsService.getSettings().isEmailNotificationsOnNewRegistration()) {
            log.debug("Email notifications disabled globally, skipping for user: {}", user.getEmail());
            return;
        }

        SmartMailingSettingsDTO settings = currentSettings.get();
        if (!settings.isEnabled()) {
            log.debug("Smart mailing disabled, skipping for user: {}", user.getEmail());
            return;
        }

        boolean isPriority = isPriorityUser(user, settings);

        // En mode IMMEDIATE ou si utilisateur prioritaire, envoyer directement
        if (settings.getNotificationMode() == NotificationMode.IMMEDIATE || isPriority) {
            sendImmediateNotification(user);
            return;
        }

        // Pour les autres modes, mettre en file d'attente
        PendingEmailNotification notification = PendingEmailNotification.builder()
                .userId(user.getId())
                .userName(user.getNom())
                .userEmail(user.getEmail())
                .userRole(user.getRole())
                .registrationDate(LocalDateTime.now())
                .priority(isPriority)
                .build();

        notificationRepository.save(notification);
        log.info("Notification queued for user: {} (mode: {})", user.getEmail(), settings.getNotificationMode());

        // En mode SMART, vérifier si on doit déclencher un envoi groupé
        if (settings.getNotificationMode() == NotificationMode.SMART) {
            checkAndTriggerSmartBatch(settings);
        }
        // En mode BATCHED, vérifier le seuil
        else if (settings.getNotificationMode() == NotificationMode.BATCHED) {
            checkAndTriggerBatch(settings);
        }
    }

    private boolean isPriorityUser(User user, SmartMailingSettingsDTO settings) {
        if (user.getRole() == Role.ENTREPRISE && settings.isPrioritizeEnterprise()) {
            return true;
        }
        if (user.getRole() == Role.ALUMNI && settings.isPrioritizeAlumni()) {
            return true;
        }
        return false;
    }

    private void sendImmediateNotification(User user) {
        List<String> adminEmails = administrateurRepository.findAllEmails();
        if (adminEmails.isEmpty()) {
            log.warn("No admin emails found for immediate notification");
            return;
        }

        String htmlContent = buildSingleRegistrationEmail(user);
        String subject = "🆕 Nouvelle inscription - " + formatRole(user.getRole().name());

        for (String adminEmail : adminEmails) {
            try {
                sendHtmlEmail(adminEmail, subject, htmlContent);
                log.info("Immediate notification sent to {} for user: {}", adminEmail, user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send immediate notification to {}: {}", adminEmail, e.getMessage());
            }
        }
    }

    private void checkAndTriggerSmartBatch(SmartMailingSettingsDTO settings) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRegistrations = notificationRepository.countRegistrationsSince(oneHourAgo);

        // Si le nombre d'inscriptions dépasse le seuil, on reste en mode groupé
        // Sinon, on traite comme des emails individuels
        if (recentRegistrations < settings.getSmartThresholdPerHour()) {
            // Peu d'activité, traiter les notifications en attente individuellement
            processIndividualNotifications();
        }
        // Sinon, attendre le scheduler pour l'envoi groupé
    }

    private void checkAndTriggerBatch(SmartMailingSettingsDTO settings) {
        long pendingCount = notificationRepository.countPendingNotifications();
        
        if (pendingCount >= settings.getBatchThreshold()) {
            processBatchNotifications();
        }
    }

    @Transactional
    public void processIndividualNotifications() {
        List<PendingEmailNotification> pending = notificationRepository
                .findByProcessedFalseAndPriorityFalseOrderByCreatedAtAsc();

        if (pending.isEmpty()) return;

        List<String> adminEmails = administrateurRepository.findAllEmails();
        if (adminEmails.isEmpty()) return;

        for (PendingEmailNotification notification : pending) {
            String htmlContent = buildSingleRegistrationEmailFromNotification(notification);
            String subject = "🆕 Nouvelle inscription - " + formatRole(notification.getUserRole().name());

            for (String adminEmail : adminEmails) {
                try {
                    sendHtmlEmail(adminEmail, subject, htmlContent);
                } catch (Exception e) {
                    log.error("Failed to send individual notification: {}", e.getMessage());
                }
            }

            notification.setProcessed(true);
            notification.setProcessedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        log.info("Processed {} individual notifications", pending.size());
    }

    @Transactional
    public void processBatchNotifications() {
        List<PendingEmailNotification> pending = notificationRepository
                .findByProcessedFalseOrderByCreatedAtAsc();

        if (pending.isEmpty()) {
            log.debug("No pending notifications to process in batch");
            return;
        }

        List<String> adminEmails = administrateurRepository.findAllEmails();
        if (adminEmails.isEmpty()) {
            log.warn("No admin emails found for batch notification");
            return;
        }

        String htmlContent = buildBatchEmail(pending);
        String subject = "📋 Résumé des inscriptions - " + pending.size() + " nouvelle(s) demande(s)";

        for (String adminEmail : adminEmails) {
            try {
                sendHtmlEmail(adminEmail, subject, htmlContent);
                log.info("Batch notification sent to {} for {} users", adminEmail, pending.size());
            } catch (Exception e) {
                log.error("Failed to send batch notification to {}: {}", adminEmail, e.getMessage());
            }
        }

        // Marquer toutes les notifications comme traitées
        pending.forEach(n -> {
            n.setProcessed(true);
            n.setProcessedAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(pending);

        log.info("Batch processed: {} notifications", pending.size());
    }

    @Transactional
    public void processHourlyDigest() {
        processBatchNotifications();
    }

    @Transactional
    public void processDailyDigest() {
        processBatchNotifications();
    }

    private String buildSingleRegistrationEmail(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String registrationDate = LocalDateTime.now().format(formatter);

        return buildEmailTemplate(
                "Nouvelle inscription en attente",
                String.format("""
                    <div class="user-card">
                        <div class="user-info">
                            <div class="user-avatar">%s</div>
                            <div class="user-details">
                                <strong>%s</strong>
                                <span class="email">%s</span>
                                <span class="role badge-%s">%s</span>
                            </div>
                        </div>
                        <div class="registration-date">
                            <small>📅 Inscrit le %s</small>
                        </div>
                    </div>
                """,
                        getInitials(user.getNom()),
                        user.getNom(),
                        user.getEmail(),
                        user.getRole().name().toLowerCase(),
                        formatRole(user.getRole().name()),
                        registrationDate
                ),
                "1 utilisateur en attente d'approbation"
        );
    }

    private String buildSingleRegistrationEmailFromNotification(PendingEmailNotification notification) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return buildEmailTemplate(
                "Nouvelle inscription en attente",
                String.format("""
                    <div class="user-card">
                        <div class="user-info">
                            <div class="user-avatar">%s</div>
                            <div class="user-details">
                                <strong>%s</strong>
                                <span class="email">%s</span>
                                <span class="role badge-%s">%s</span>
                            </div>
                        </div>
                        <div class="registration-date">
                            <small>📅 Inscrit le %s</small>
                        </div>
                    </div>
                """,
                        getInitials(notification.getUserName()),
                        notification.getUserName(),
                        notification.getUserEmail(),
                        notification.getUserRole().name().toLowerCase(),
                        formatRole(notification.getUserRole().name()),
                        notification.getRegistrationDate().format(formatter)
                ),
                "1 utilisateur en attente d'approbation"
        );
    }

    private String buildBatchEmail(List<PendingEmailNotification> notifications) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Grouper par rôle
        var byRole = notifications.stream()
                .collect(Collectors.groupingBy(PendingEmailNotification::getUserRole));

        StringBuilder stats = new StringBuilder();
        stats.append("<div class=\"stats-container\">");
        for (var entry : byRole.entrySet()) {
            stats.append(String.format("""
                <div class="stat-item">
                    <span class="stat-count">%d</span>
                    <span class="stat-label">%s</span>
                </div>
            """, entry.getValue().size(), formatRole(entry.getKey().name())));
        }
        stats.append("</div>");

        StringBuilder userList = new StringBuilder();
        userList.append("<div class=\"users-list\">");
        
        for (PendingEmailNotification n : notifications) {
            String priorityBadge = n.isPriority() ? "<span class=\"priority-badge\">⚡ Prioritaire</span>" : "";
            userList.append(String.format("""
                <div class="user-card compact">
                    <div class="user-info">
                        <div class="user-avatar small">%s</div>
                        <div class="user-details">
                            <strong>%s</strong> %s
                            <span class="email">%s</span>
                        </div>
                    </div>
                    <div class="user-meta">
                        <span class="role badge-%s">%s</span>
                        <small>%s</small>
                    </div>
                </div>
            """,
                    getInitials(n.getUserName()),
                    n.getUserName(),
                    priorityBadge,
                    n.getUserEmail(),
                    n.getUserRole().name().toLowerCase(),
                    formatRole(n.getUserRole().name()),
                    n.getRegistrationDate().format(formatter)
            ));
        }
        userList.append("</div>");

        return buildEmailTemplate(
                "Résumé des nouvelles inscriptions",
                stats.toString() + userList.toString(),
                notifications.size() + " utilisateur(s) en attente d'approbation"
        );
    }

    private String buildEmailTemplate(String title, String content, String summary) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f5f5f5; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #dc2626, #991b1b); color: white; padding: 30px; text-align: center; }
                    .header h1 { font-size: 24px; margin-bottom: 8px; }
                    .header p { opacity: 0.9; font-size: 14px; }
                    .content { padding: 30px; }
                    .stats-container { display: flex; justify-content: space-around; margin-bottom: 24px; padding: 20px; background: #f9fafb; border-radius: 8px; }
                    .stat-item { text-align: center; }
                    .stat-count { display: block; font-size: 28px; font-weight: bold; color: #dc2626; }
                    .stat-label { font-size: 12px; color: #6b7280; text-transform: uppercase; }
                    .users-list { }
                    .user-card { background: #f9fafb; border-radius: 8px; padding: 16px; margin-bottom: 12px; border-left: 4px solid #dc2626; }
                    .user-card.compact { padding: 12px; display: flex; justify-content: space-between; align-items: center; }
                    .user-info { display: flex; align-items: center; gap: 12px; }
                    .user-avatar { width: 48px; height: 48px; background: #dc2626; color: white; border-radius: 50%%; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 16px; }
                    .user-avatar.small { width: 36px; height: 36px; font-size: 12px; }
                    .user-details { display: flex; flex-direction: column; }
                    .user-details strong { color: #111827; }
                    .user-details .email { color: #6b7280; font-size: 13px; }
                    .user-meta { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
                    .role { display: inline-block; padding: 4px 10px; border-radius: 12px; font-size: 11px; font-weight: 600; text-transform: uppercase; }
                    .badge-etudiant { background: #dbeafe; color: #1d4ed8; }
                    .badge-alumni { background: #dcfce7; color: #15803d; }
                    .badge-entreprise { background: #fef3c7; color: #b45309; }
                    .badge-enseignant { background: #f3e8ff; color: #7c3aed; }
                    .priority-badge { background: #fef3c7; color: #b45309; font-size: 10px; padding: 2px 6px; border-radius: 4px; margin-left: 8px; }
                    .registration-date { margin-top: 12px; color: #6b7280; }
                    .cta-container { text-align: center; margin-top: 24px; }
                    .cta-button { display: inline-block; background: #dc2626; color: white; padding: 14px 32px; border-radius: 8px; text-decoration: none; font-weight: 600; transition: background 0.2s; }
                    .cta-button:hover { background: #b91c1c; }
                    .footer { background: #f9fafb; padding: 20px; text-align: center; color: #6b7280; font-size: 12px; }
                    .footer a { color: #dc2626; text-decoration: none; }
                    .summary { background: #fef2f2; border: 1px solid #fecaca; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; text-align: center; color: #991b1b; font-weight: 500; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                        <p>EspritConnect - Gestion des utilisateurs</p>
                    </div>
                    <div class="content">
                        <div class="summary">%s</div>
                        %s
                        <div class="cta-container">
                            <a href="%s/admin/user-approval" class="cta-button">
                                Gérer les approbations
                            </a>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement par EspritConnect.</p>
                        <p><a href="%s/admin/user-approval?tab=settings">Gérer les préférences de notification</a></p>
                    </div>
                </div>
            </body>
            </html>
        """, title, summary, content, frontendUrl, frontendUrl);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) 
            throws MessagingException, UnsupportedEncodingException {
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

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private SmartMailingSettingsDTO mapToDTO(SmartMailingSettings entity) {
        return SmartMailingSettingsDTO.builder()
                .enabled(entity.isEnabled())
                .batchThreshold(entity.getBatchThreshold())
                .batchWindowMinutes(entity.getBatchWindowMinutes())
                .notificationMode(entity.getNotificationMode())
                .dailyDigestHour(entity.getDailyDigestHour())
                .prioritizeEnterprise(entity.isPrioritizeEnterprise())
                .prioritizeAlumni(entity.isPrioritizeAlumni())
                .smartThresholdPerHour(entity.getSmartThresholdPerHour())
                .dashboardNotificationsEnabled(entity.isDashboardNotificationsEnabled())
                .build();
    }

    public long getPendingNotificationsCount() {
        return notificationRepository.countPendingNotifications();
    }

    @Transactional
    public void cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        notificationRepository.deleteByProcessedTrueAndProcessedAtBefore(cutoff);
        log.info("Cleaned up notifications older than {} days", daysToKeep);
    }
}

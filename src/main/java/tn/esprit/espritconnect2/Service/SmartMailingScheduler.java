package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.DTO.SmartMailingSettingsDTO;
import tn.esprit.espritconnect2.Entitie.SmartMailingSettings.NotificationMode;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmartMailingScheduler {

    private final SmartMailingService smartMailingService;

    /**
     * Vérifie toutes les 5 minutes si des notifications en batch doivent être envoyées
     * (pour le mode BATCHED avec délai temporel)
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkBatchWindowExpired() {
        SmartMailingSettingsDTO settings = smartMailingService.getSettings();
        
        if (!settings.isEnabled()) return;
        
        if (settings.getNotificationMode() == NotificationMode.BATCHED ||
            settings.getNotificationMode() == NotificationMode.SMART) {
            
            long pendingCount = smartMailingService.getPendingNotificationsCount();
            
            if (pendingCount > 0) {
                // En mode BATCHED, vérifier si le délai est dépassé ou le seuil atteint
                if (settings.getNotificationMode() == NotificationMode.BATCHED) {
                    if (pendingCount >= settings.getBatchThreshold()) {
                        log.info("Batch threshold reached ({}/{}), processing notifications", 
                                pendingCount, settings.getBatchThreshold());
                        smartMailingService.processBatchNotifications();
                    }
                }
                // En mode SMART, la logique est gérée dans SmartMailingService
            }
        }
    }

    /**
     * Exécute le digest horaire (toutes les heures à :00)
     */
    @Scheduled(cron = "0 0 * * * *") // Toutes les heures à :00
    public void processHourlyDigest() {
        SmartMailingSettingsDTO settings = smartMailingService.getSettings();
        
        if (!settings.isEnabled()) return;
        
        if (settings.getNotificationMode() == NotificationMode.HOURLY_DIGEST) {
            log.info("Processing hourly digest at {}", LocalDateTime.now());
            smartMailingService.processHourlyDigest();
        }
    }

    /**
     * Exécute le digest quotidien à l'heure configurée
     * Vérifie chaque heure si c'est le bon moment
     */
    @Scheduled(cron = "0 0 * * * *") // Toutes les heures à :00
    public void processDailyDigest() {
        SmartMailingSettingsDTO settings = smartMailingService.getSettings();
        
        if (!settings.isEnabled()) return;
        
        if (settings.getNotificationMode() == NotificationMode.DAILY_DIGEST) {
            int currentHour = LocalTime.now().getHour();
            
            if (currentHour == settings.getDailyDigestHour()) {
                log.info("Processing daily digest at {}h", currentHour);
                smartMailingService.processDailyDigest();
            }
        }
    }

    /**
     * Traitement des notifications en attente en mode SMART
     * Vérifie le volume et adapte la stratégie
     */
    @Scheduled(fixedRate = 600000) // Toutes les 10 minutes
    public void processSmartMode() {
        SmartMailingSettingsDTO settings = smartMailingService.getSettings();
        
        if (!settings.isEnabled()) return;
        
        if (settings.getNotificationMode() == NotificationMode.SMART) {
            long pendingCount = smartMailingService.getPendingNotificationsCount();
            
            if (pendingCount == 0) return;
            
            // Si on a dépassé le seuil par heure OU si des notifications sont en attente depuis > batchWindowMinutes
            if (pendingCount >= settings.getBatchThreshold()) {
                log.info("SMART mode: threshold reached, sending batch email for {} notifications", pendingCount);
                smartMailingService.processBatchNotifications();
            } else {
                // Peu de notifications, les envoyer individuellement si elles sont anciennes
                log.debug("SMART mode: {} pending notifications, checking if individual send needed", pendingCount);
                smartMailingService.processIndividualNotifications();
            }
        }
    }

    /**
     * Nettoyage hebdomadaire des anciennes notifications traitées
     */
    @Scheduled(cron = "0 0 3 * * SUN") // Chaque dimanche à 3h du matin
    public void cleanupOldNotifications() {
        log.info("Starting weekly cleanup of old notifications");
        smartMailingService.cleanupOldNotifications(30); // Garde 30 jours d'historique
    }

    /**
     * Traitement des notifications avec délai temporel dépassé
     * Si des notifications sont en attente depuis plus de batchWindowMinutes
     */
    @Scheduled(fixedRate = 60000) // Toutes les minutes
    public void checkTimeWindowExpired() {
        SmartMailingSettingsDTO settings = smartMailingService.getSettings();
        
        if (!settings.isEnabled()) return;
        
        // Pour les modes BATCHED et SMART, vérifier si le délai temporel est dépassé
        if (settings.getNotificationMode() == NotificationMode.BATCHED ||
            settings.getNotificationMode() == NotificationMode.SMART) {
            
            long pendingCount = smartMailingService.getPendingNotificationsCount();
            
            if (pendingCount > 0 && pendingCount < settings.getBatchThreshold()) {
                // Il y a des notifications mais pas assez pour le batch
                // Vérifier si le temps d'attente max est dépassé
                // Cette logique sera gérée par le service qui vérifie les timestamps
            }
        }
    }
}

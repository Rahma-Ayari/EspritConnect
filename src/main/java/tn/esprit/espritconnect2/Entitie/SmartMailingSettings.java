package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "smart_mailing_settings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SmartMailingSettings {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // Seuil minimum d'inscriptions pour déclencher un email groupé
    @Column(nullable = false)
    @Builder.Default
    private int batchThreshold = 5;

    // Temps d'attente en minutes avant d'envoyer un email groupé
    @Column(nullable = false)
    @Builder.Default
    private int batchWindowMinutes = 30;

    // Mode de notification: IMMEDIATE, BATCHED, HOURLY_DIGEST, DAILY_DIGEST, SMART
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationMode notificationMode = NotificationMode.SMART;

    // Heure d'envoi du digest quotidien (0-23)
    @Column(nullable = false)
    @Builder.Default
    private int dailyDigestHour = 8;

    // Priorité immédiate pour les entreprises
    @Column(nullable = false)
    @Builder.Default
    private boolean prioritizeEnterprise = true;

    // Priorité immédiate pour les alumni
    @Column(nullable = false)
    @Builder.Default
    private boolean prioritizeAlumni = false;

    // Nombre max d'inscriptions par heure avant de passer en mode groupé (mode SMART)
    @Column(nullable = false)
    @Builder.Default
    private int smartThresholdPerHour = 10;

    // Activer les notifications en temps réel dans le dashboard (indépendant des emails)
    @Column(nullable = false)
    @Builder.Default
    private boolean dashboardNotificationsEnabled = true;

    public enum NotificationMode {
        IMMEDIATE,      // Email immédiat pour chaque inscription
        BATCHED,        // Regroupe après X inscriptions ou Y minutes
        HOURLY_DIGEST,  // Résumé toutes les heures
        DAILY_DIGEST,   // Résumé quotidien
        SMART           // Adaptatif selon le volume
    }
}

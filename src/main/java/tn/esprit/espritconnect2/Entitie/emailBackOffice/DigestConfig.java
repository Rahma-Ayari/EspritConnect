package tn.esprit.espritconnect2.Entitie.emailBackOffice;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


/**
 * Entité qui stocke la configuration de l'Activity Digest.
 * Il n'y a qu'UNE SEULE configuration (id = 1 toujours).
 * C'est ce que l'admin modifie via l'interface "Activity Digest".
 */
@Entity
@Table(name = "digest_config")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DigestConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // IMPORTANT : on force id=1 (config unique)

    // L'objet de l'email (ex: "What's new on Esprit")
    @Column(nullable = false, length = 254)
    private String sujet;

    // URL de l'image bannière de l'email
    private String bannerUrl;

    // Fréquence d'envoi : "DAILY" ou "WEEKLY"
    private String frequence;

    // Le digest est-il actif ou non ?
    @Column(nullable = false)
    private Boolean actif; // ← true = on envoie les emails, false = on n'envoie pas

    // --- Template HTML sauvegardé depuis le builder ---
    @Lob // ← Large Object (peut stocker beaucoup de texte)
    @Column(columnDefinition = "LONGTEXT") // ← Type MySQL pour grands textes
    private String templateHtml; // ← Le HTML personnalisé de l'email (créé par l'admin)
    // --- Sections auto (checkboxes) : quelles sections afficher dans l'email ---
    @Column(nullable = false) private boolean businessDirectoryPosts; // ← Posts business
    @Column(nullable = false) private boolean recentlyJoinedMembers; // ← Nouveaux membres
    @Column(nullable = false) private boolean latestEvents; // ← Derniers événements
    @Column(nullable = false) private boolean latestFeedPosts; // ← Posts du feed
    @Column(nullable = false) private boolean latestJobPosts; // ← Offres d'emploi
    @Column(nullable = false) private boolean includePlatformContact; // ← Infos de contact

    // --- Pour calculer la fenêtre des "nouveaux contenus" ---
    private LocalDateTime lastSentAt; // ← Quand le dernier email a été envoyé

    // ID de la liste de diffusion sélectionnée pour l'envoi
    private Long mailingListId;

    // URL publique de ton frontend (pour générer les liens)
    @Column(nullable = false)
    private String frontendBaseUrl;

    // ── Méthode appelée AUTOMATIQUEMENT avant toute insertion en BDD ──
    @PrePersist
    public void prePersist() {
        // Si pas d'id → forcer id=1 (config unique !)
        if (id == null) id = 1L;

        // Valeurs par défaut si l'admin n'a rien mis
        if (sujet == null || sujet.isBlank()) sujet = "What's new on Esprit";
        if (frequence == null || frequence.isBlank()) frequence = "WEEKLY";
        if (actif == null) actif = true;


        // Si AUCUNE section n'est cochée → activer les sections par défaut
        if (!businessDirectoryPosts && !recentlyJoinedMembers && !latestEvents
                && !latestFeedPosts && !latestJobPosts && !includePlatformContact) {
            recentlyJoinedMembers = true; // ← On coche celles-ci par défaut
            latestEvents = true;
            latestFeedPosts = true;
            latestJobPosts = true;
            includePlatformContact = true;
        }
        if (frontendBaseUrl == null || frontendBaseUrl.isBlank()) {
            frontendBaseUrl = "http://localhost:4200";
        }
    }
}
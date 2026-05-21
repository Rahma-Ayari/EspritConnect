package tn.esprit.espritconnect2.Entitie.emailBackOffice;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Configuration de l'email automatique "Anniversaire".
 * On garde une ligne unique (id=1) pour simplifier l'admin (comme DigestConfig).
 */
@Entity
@Table(name = "birthday_email_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BirthdayEmailSettings {

    @Id
    private Long id;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false, length = 254)
    private String subject;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String templateHtml;

    /** Heure d'envoi (0-23) */
    @Column(nullable = false)
    private Integer sendHour;

    private LocalDateTime lastRunAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = 1L;
        if (active == null) active = false;
        if (sendHour == null) sendHour = 8;
        if (subject == null || subject.isBlank()) {
            subject = "Joyeux anniversaire depuis EspritConnect";
        }
        if (templateHtml == null || templateHtml.isBlank()) {
            templateHtml = "<p>Bonjour <b>{{nom}}</b>, toute l'équipe te souhaite un joyeux anniversaire !</p>";
        }
    }
}
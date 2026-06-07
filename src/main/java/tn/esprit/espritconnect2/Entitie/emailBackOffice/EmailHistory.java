package tn.esprit.espritconnect2.Entitie.emailBackOffice;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailDeliveryStatus;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailHistoryType;

import java.time.LocalDateTime;

/**
 * Historique d'envoi (pour l'écran "History & statistics").
 * Une ligne = une tentative d'envoi à UN destinataire.
 */
@Entity
@Table(name = "email_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EmailHistoryType type;

    /** Lien vers la campagne (nullable pour les tests anniversaire) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private EmailCampaign campaign;

    @Column(nullable = false, length = 254)
    private String toEmail;

    @Column(nullable = false, length = 254)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailDeliveryStatus deliveryStatus;

    @Lob
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
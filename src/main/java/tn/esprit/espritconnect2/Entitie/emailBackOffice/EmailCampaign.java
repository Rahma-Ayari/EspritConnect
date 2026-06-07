package tn.esprit.espritconnect2.Entitie.emailBackOffice;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailCampaignStatus;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.RecipientScope;

import java.time.LocalDateTime;

/**
 * Campagne email admin ("Message users" dans l'ancienne interface).
 * Une ligne = un email de masse (sujet + HTML) + cible (étudiants, users, liste).
 */
@Entity
@Table(name = "email_campaign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sujet affiché dans la boîte mail */
    @Column(nullable = false, length = 254)
    private String subject;

    /** Corps HTML (comme ton builder digest) */
    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String htmlBody;

    /** Expéditeur affiché (doit matcher ton SMTP autorisé si Gmail impose) */
    @Column(nullable = false, length = 254)
    private String fromEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailCampaignStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecipientScope recipientScope;

    /** Obligatoire si recipientScope = MAILING_LIST */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mailing_list_id")
    private MailingList mailingList;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = EmailCampaignStatus.DRAFT;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

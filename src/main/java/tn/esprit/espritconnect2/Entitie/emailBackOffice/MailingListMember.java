package tn.esprit.espritconnect2.Entitie.emailBackOffice;

import jakarta.persistence.*;
import lombok.*;

/**
 * Membre d'une liste de diffusion.
 * On stocke au minimum l'email (obligatoire pour envoyer).
 */
@Entity
@Table(name = "mailing_list_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailingListMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mailing_list_id", nullable = false)
    private MailingList mailingList;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 120)
    private String nom;

    /** Optionnel : lien vers users.id (UUID) si tu veux synchroniser plus tard */
    @Column(length = 36)
    private String userUuid;
}
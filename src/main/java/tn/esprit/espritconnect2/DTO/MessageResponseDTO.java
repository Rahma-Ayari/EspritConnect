package tn.esprit.espritconnect2.DTO;

import lombok.*;

import java.util.Date;

/**
 * DTO renvoyé au client après création, lecture ou mise à jour d'un message.
 * On inclut les infos du profil lié sous forme d'id (pas l'objet entier).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponseDTO {

    // Identifiant interne du message
    private Long idMessage;

    // Identifiant externe de l'utilisateur
    private String userId;

    // Date d'envoi du message (générée automatiquement côté serveur)
    private Date dateEnvoi;

    // Statut de lecture du message
    private Boolean lu;

    // Nom ou identifiant de l'expéditeur
    private String expediteur;

    // Contenu du message
    private String contenu;

    // On expose seulement l'id du profil lié, pas l'objet entier
    // pour éviter les références circulaires et alléger la réponse
    private Long idProfil;
}
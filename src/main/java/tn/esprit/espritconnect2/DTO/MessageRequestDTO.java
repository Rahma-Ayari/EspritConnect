package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO reçu du client pour créer ou modifier un message.
 * On passe l'idProfil pour lier le message à un profil existant.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDTO {

    // L'identifiant externe de l'utilisateur (ex : depuis JWT)
    @NotBlank(message = "Le userId est obligatoire")
    private String userId;

    // Nom ou identifiant de l'expéditeur du message
    @NotBlank(message = "L'expéditeur est obligatoire")
    private String expediteur;

    // Contenu textuel du message
    @NotBlank(message = "Le contenu est obligatoire")
    private String contenu;

    // Le message est-il lu ? (false par défaut à la création)
    private Boolean lu = false;

    // L'id du profil auquel ce message est rattaché (clé étrangère)
    @NotNull(message = "L'id du profil est obligatoire")
    private Long idProfil;
}
package tn.esprit.espritconnect2.DTO;

import lombok.*;

/**
 * DTO utilisé pour envoyer les données d'un admin vers le client (réponse).
 * On n'expose JAMAIS le mot de passe dans la réponse → sécurité essentielle.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResponseDTO {

    private Long idAdmin;
    private String nom;
    private String email;
    private String role;
    // Note : pas de password dans la réponse !
}
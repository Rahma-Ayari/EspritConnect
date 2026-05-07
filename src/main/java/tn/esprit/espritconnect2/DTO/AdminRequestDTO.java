package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO utilisé pour recevoir les données d'un admin depuis le client (création/modification).
 * On ne reçoit JAMAIS l'entité directement → sécurité et contrôle des champs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    // Le rôle de l'admin (ex: "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
    @NotBlank(message = "Le rôle est obligatoire")
    private String role;
}
package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.espritconnect2.Entitie.Niveau;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    @NotBlank(message = "Le nom est requis")
    private String nom;

    @Email(message = "Format email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Minimum 6 caractères")
    private String password;

    @NotNull(message = "Le niveau est requis")
    private Niveau niveau;

    @NotBlank(message = "La filière est requise")
    private String filiere;

    private String diplome;
    private String photo;
}

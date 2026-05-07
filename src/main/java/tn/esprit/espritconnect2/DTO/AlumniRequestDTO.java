package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlumniRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6)
    private String password;

    @NotNull(message = "L'année promotion est obligatoire")
    private Integer anneePromotion;

    @NotBlank(message = "Le domaine est obligatoire")
    private String domaine;

    private Boolean disponibleMentorat;

    private String entrepriseActuelle;
}
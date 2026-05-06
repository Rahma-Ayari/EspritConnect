package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.espritconnect2.Entitie.Niveau;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50)
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6)
    private String password;

    @NotNull(message = "Le niveau est obligatoire")
    private Niveau niveau;

    @NotBlank(message = "La filière est obligatoire")
    private String filiere;

    private Integer scoreReadiness;
}
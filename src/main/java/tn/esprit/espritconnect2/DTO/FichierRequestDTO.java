package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.espritconnect2.Entitie.Type;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FichierRequestDTO {
    @NotBlank
    private String nom;

    @NotBlank
    private String url;

    @NotNull
    private Type typeFichier;

    @NotNull
    @Positive
    private Long taille;

    private Long etudiantId;
    private Long alumniId;
    private Long candidatureId;
}

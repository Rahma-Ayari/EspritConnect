package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.esprit.espritconnect2.Entitie.Type;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class OffreRequestDTO {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "Le type d'offre est obligatoire")
    private Type typeOffre;

    private String localisation;

    @NotNull(message = "L'ID de l'entreprise est obligatoire")
    private Long entrepriseId;
}

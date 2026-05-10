package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AICareerAssistantRequestDTO {

    @NotBlank(message = "Le modèle est obligatoire")
    private String modele;

    @NotBlank(message = "La fonctionnalité est obligatoire")
    private String fonctionnalite;

    private String versionAssistant;

    private Boolean actif;

    private Float scorePrecision;
}
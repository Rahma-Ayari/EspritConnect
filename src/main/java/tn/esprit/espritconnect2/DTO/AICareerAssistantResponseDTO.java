package tn.esprit.espritconnect2.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AICareerAssistantResponseDTO {

    private Long idAssistant;

    private String modele;

    private String fonctionnalite;

    private String versionAssistant;

    private Boolean actif;

    private Float scorePrecision;
}
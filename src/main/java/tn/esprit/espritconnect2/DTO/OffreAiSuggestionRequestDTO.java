package tn.esprit.espritconnect2.DTO;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.Type;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffreAiSuggestionRequestDTO {
    private String titre;
    private Type typeOffre;
    private String domaine;
    private String localisation;
    private String briefNotes;
}

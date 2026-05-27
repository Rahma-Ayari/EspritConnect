package tn.esprit.espritconnect2.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffreAiSuggestionResponseDTO {
    private String suggestedDescription;
    private List<String> suggestedSkills;
    private String aiDisclaimer;
}

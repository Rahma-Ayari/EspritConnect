package tn.esprit.espritconnect2.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIDescriptionResponse {
    private String description;
    private List<String> suggestedSkills;
}

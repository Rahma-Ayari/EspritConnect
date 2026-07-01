package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import java.util.List;

@Data
public class AIJobGenerateResponseDTO {
    private String description;
    private String responsibilities;
    private String requirements;
    private String benefits;
    private List<String> keywords;
    private String recruitmentText;
    private String aiDisclaimer;
    private String suggestedTitle;
    private List<String> suggestedSkills;
}

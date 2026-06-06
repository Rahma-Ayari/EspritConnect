package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import java.util.List;

@Data
public class AIJobGenerateRequestDTO {
    private String title;
    private List<String> skills;
    private String experienceLevel;
    private String contractType;
    private String department;
    private String location;
    private String additionalPrompt;
    /** Output language code: en, fr, ar */
    private String outputLanguage;
}

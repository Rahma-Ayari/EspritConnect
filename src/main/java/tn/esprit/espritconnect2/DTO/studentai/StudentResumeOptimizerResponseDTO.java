package tn.esprit.espritconnect2.DTO.studentai;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tn.esprit.espritconnect2.DTO.AiResponseMetaDTO;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentResumeOptimizerResponseDTO extends AiResponseMetaDTO {
    private Integer atsScore;
    private String atsLabel;
    private String optimizedSummary;
    private List<String> improvedBulletPoints;
    private List<String> atsKeywords;
    private List<String> missingSkills;
    private List<String> suggestions;
    private Long offreId;
    private String jobTitle;
}

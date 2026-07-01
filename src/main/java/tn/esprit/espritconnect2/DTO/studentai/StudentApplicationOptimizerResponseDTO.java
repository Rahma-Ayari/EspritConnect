package tn.esprit.espritconnect2.DTO.studentai;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tn.esprit.espritconnect2.DTO.AiResponseMetaDTO;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentApplicationOptimizerResponseDTO extends AiResponseMetaDTO {
    private Integer matchScore;
    private String matchLabel;
    private Integer skillsScore;
    private Integer readinessScore;
    private List<String> missingSkills;
    private String optimizedSummary;
    private List<String> improvedBulletPoints;
    private String coverLetter;
    private List<String> atsKeywords;
    private List<String> readinessChecklist;
    private String recommendation;
    private Long offreId;
    private String jobTitle;
}

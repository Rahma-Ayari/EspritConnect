package tn.esprit.espritconnect2.DTO.studentai;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tn.esprit.espritconnect2.DTO.AiResponseMetaDTO;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentJobMatchResponseDTO extends AiResponseMetaDTO {
    private Integer overallScore;
    private Integer skillsScore;
    private Integer projectsScore;
    private Integer experienceScore;
    private Integer educationScore;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private List<String> improvementSuggestions;
    private String recommendation;
    private Long offreId;
    private String jobTitle;
}

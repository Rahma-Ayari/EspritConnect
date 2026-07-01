package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiMatchCandidateResponseDTO extends AiResponseMetaDTO {
    private Integer overallScore;
    private Integer skillsScore;
    private Integer experienceScore;
    private Integer educationScore;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private List<String> strengths;
    private List<String> weaknesses;
    private String recommendation;
    private Long etudiantId;
    private Long offreId;
    private String etudiantNom;
}

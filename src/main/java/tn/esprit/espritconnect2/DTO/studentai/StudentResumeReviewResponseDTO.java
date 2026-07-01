package tn.esprit.espritconnect2.DTO.studentai;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tn.esprit.espritconnect2.DTO.AiResponseMetaDTO;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentResumeReviewResponseDTO extends AiResponseMetaDTO {
    private Integer atsScore;
    private String atsLabel;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> formattingIssues;
    private List<String> keywordGaps;
    private List<String> suggestions;
    private List<String> extractedSkills;
    private List<String> extractedExperience;
    private List<String> extractedEducation;
}

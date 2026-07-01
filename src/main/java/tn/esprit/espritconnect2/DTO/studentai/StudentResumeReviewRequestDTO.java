package tn.esprit.espritconnect2.DTO.studentai;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentResumeReviewRequestDTO extends StudentAiBaseRequestDTO {
    private String resumeText;
    private String targetRole;
}

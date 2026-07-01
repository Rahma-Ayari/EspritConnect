package tn.esprit.espritconnect2.DTO.studentai;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentApplicationOptimizerRequestDTO extends StudentAiBaseRequestDTO {
    private Long offreId;
    private String resumeText;
}

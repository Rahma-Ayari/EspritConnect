package tn.esprit.espritconnect2.DTO.studentai;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentCareerAdviceRequestDTO extends StudentAiBaseRequestDTO {
    private String question;
}

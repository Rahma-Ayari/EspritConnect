package tn.esprit.espritconnect2.DTO.studentai;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tn.esprit.espritconnect2.DTO.AiResponseMetaDTO;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentCoverLetterResponseDTO extends AiResponseMetaDTO {
    private String letter;
}

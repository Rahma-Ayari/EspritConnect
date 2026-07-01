package tn.esprit.espritconnect2.DTO.studentai;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tn.esprit.espritconnect2.DTO.AiResponseMetaDTO;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentCareerAdviceResponseDTO extends AiResponseMetaDTO {
    private String answer;
    private List<String> actionItems;
    private List<String> resources;
}

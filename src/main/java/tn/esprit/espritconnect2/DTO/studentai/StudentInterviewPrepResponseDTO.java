package tn.esprit.espritconnect2.DTO.studentai;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tn.esprit.espritconnect2.DTO.AiResponseMetaDTO;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentInterviewPrepResponseDTO extends AiResponseMetaDTO {
    private List<String> technicalQuestions;
    private List<String> behavioralQuestions;
    private List<String> hrQuestions;
    private List<String> suggestedAnswers;
    private List<String> interviewTips;
}

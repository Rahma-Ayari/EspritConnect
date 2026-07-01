package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiCandidateSummaryResponseDTO extends AiResponseMetaDTO {
    private String summary;
    private Integer overallScore;
    private String recommendation;
}

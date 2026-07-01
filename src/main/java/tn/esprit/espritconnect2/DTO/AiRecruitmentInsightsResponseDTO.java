package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiRecruitmentInsightsResponseDTO extends AiResponseMetaDTO {
    private List<String> insights;
    private String summary;
    private List<String> recommendations;
}

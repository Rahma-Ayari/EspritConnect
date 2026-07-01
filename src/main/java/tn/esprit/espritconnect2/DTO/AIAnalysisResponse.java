package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import java.util.List;

@Data
public class AIAnalysisResponse {
    private String overallSummary;
    private List<UserActivitySummary> userSummaries;
    private List<String> recommendations;
}

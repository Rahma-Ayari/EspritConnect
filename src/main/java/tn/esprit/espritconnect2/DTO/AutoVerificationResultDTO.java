package tn.esprit.espritconnect2.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoVerificationResultDTO {
    private int totalScore;
    private List<ScoreBreakdown> breakdown;
    private String recommendation;
    private String recommendationText;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreBreakdown {
        private String criteria;
        private int points;
        private int maxPoints;
        private boolean passed;
    }
}

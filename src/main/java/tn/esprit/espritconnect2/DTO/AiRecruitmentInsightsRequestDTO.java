package tn.esprit.espritconnect2.DTO;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiRecruitmentInsightsRequestDTO {
    private Long entrepriseId;
    private Long offreId;
    private Integer totalOffers;
    private Integer activeOffers;
    private Integer totalApplications;
    private Double avgApplicationsPerOffer;
    private Map<String, Integer> applicationsByStatus;
    private List<Integer> topCandidateScores;
    private List<String> commonMissingSkills;
    private boolean forceRefresh;
}

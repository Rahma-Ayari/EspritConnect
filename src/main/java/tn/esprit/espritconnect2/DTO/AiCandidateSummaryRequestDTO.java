package tn.esprit.espritconnect2.DTO;

import lombok.Data;

@Data
public class AiCandidateSummaryRequestDTO {
    private Long offreId;
    private Long etudiantId;
    private Long candidatureId;
    private AiMatchCandidateResponseDTO matchData;
    private boolean forceRefresh;
}

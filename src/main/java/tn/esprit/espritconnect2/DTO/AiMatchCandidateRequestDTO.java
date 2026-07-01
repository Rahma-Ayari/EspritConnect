package tn.esprit.espritconnect2.DTO;

import lombok.Data;

@Data
public class AiMatchCandidateRequestDTO {
    private Long offreId;
    private Long etudiantId;
    private Long candidatureId;
    private boolean forceRefresh;
}

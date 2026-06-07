package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.CandidateMatchDTO;
import tn.esprit.espritconnect2.DTO.MatchingResponseDTO;

import java.util.List;

public interface IMatchingService {
    MatchingResponseDTO computeMatching(Long etudiantId, Long offreId);

    List<MatchingResponseDTO> recomputeForEtudiant(Long etudiantId);

    List<MatchingResponseDTO> getByEtudiant(Long etudiantId);

    List<MatchingResponseDTO> getTopByOffre(Long offreId, int limit);

    List<CandidateMatchDTO> getRankedCandidatesForOffre(Long offreId, int limit);
}

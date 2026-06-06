package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.CandidatureApplyMeDTO;
import tn.esprit.espritconnect2.DTO.CandidatureRequestDTO;
import tn.esprit.espritconnect2.DTO.CandidatureResponseDTO;
import tn.esprit.espritconnect2.Entitie.Status;

import java.util.List;

public interface ICandidatureService {
    CandidatureResponseDTO create(CandidatureRequestDTO dto);

    CandidatureResponseDTO createForEmail(String email, CandidatureApplyMeDTO dto);

    List<CandidatureResponseDTO> getByEtudiant(Long etudiantId);

    List<CandidatureResponseDTO> getByOffre(Long offreId);

    CandidatureResponseDTO updateStatus(Long candidatureId, Status status);

    void cancel(Long candidatureId);
}

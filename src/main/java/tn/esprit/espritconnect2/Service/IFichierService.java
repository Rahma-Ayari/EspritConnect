package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.FichierRequestDTO;
import tn.esprit.espritconnect2.DTO.FichierResponseDTO;

import java.util.List;

public interface IFichierService {
    FichierResponseDTO create(FichierRequestDTO dto);

    FichierResponseDTO attachToCandidature(Long fichierId, Long candidatureId);

    List<FichierResponseDTO> getByEtudiant(Long etudiantId);

    void delete(Long fichierId);
}

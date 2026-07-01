package tn.esprit.espritconnect2.Service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.FichierRequestDTO;
import tn.esprit.espritconnect2.DTO.FichierResponseDTO;

import java.util.List;

public interface IFichierService {
    FichierResponseDTO create(FichierRequestDTO dto);

    FichierResponseDTO attachToCandidature(Long fichierId, Long candidatureId);

    List<FichierResponseDTO> getByEtudiant(Long etudiantId);

    /** Upload and store a CV file for the currently authenticated student. */
    FichierResponseDTO uploadForEmail(String email, MultipartFile file);

    /** List the files belonging to the currently authenticated student. */
    List<FichierResponseDTO> getByEmail(String email);

    void delete(Long fichierId);
}

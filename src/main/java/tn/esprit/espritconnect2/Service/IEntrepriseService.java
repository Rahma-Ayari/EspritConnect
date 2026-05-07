package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.EntrepriseRequestDTO;
import tn.esprit.espritconnect2.DTO.EntrepriseResponseDTO;

import java.util.List;

public interface IEntrepriseService {
    EntrepriseResponseDTO createEntreprise(EntrepriseRequestDTO dto);
    List<EntrepriseResponseDTO> getAllEntreprises();
    EntrepriseResponseDTO getEntrepriseById(Long id);
    EntrepriseResponseDTO updateEntreprise(Long id, EntrepriseRequestDTO dto);
    void deleteEntreprise(Long id);
}

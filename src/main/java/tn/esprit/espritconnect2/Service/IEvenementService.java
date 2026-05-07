package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.EvenementRequestDTO;
import tn.esprit.espritconnect2.DTO.EvenementResponseDTO;

import java.util.List;

public interface IEvenementService {
    EvenementResponseDTO createEvenement(EvenementRequestDTO dto);
    List<EvenementResponseDTO> getAllEvenements();
    EvenementResponseDTO getEvenementById(Long id);
    EvenementResponseDTO updateEvenement(Long id, EvenementRequestDTO dto);
    void deleteEvenement(Long id);
}

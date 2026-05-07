package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.OffreRequestDTO;
import tn.esprit.espritconnect2.DTO.OffreResponseDTO;

import java.util.List;

public interface IOffreService {
    OffreResponseDTO createOffre(OffreRequestDTO dto);
    List<OffreResponseDTO> getAllOffres();
    OffreResponseDTO getOffreById(Long id);
    OffreResponseDTO updateOffre(Long id, OffreRequestDTO dto);
    void deleteOffre(Long id);
}

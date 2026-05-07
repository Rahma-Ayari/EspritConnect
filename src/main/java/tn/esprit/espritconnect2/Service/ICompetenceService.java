package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.CompetenceRequestDTO;
import tn.esprit.espritconnect2.DTO.CompetenceResponseDTO;
import java.util.List;

public interface ICompetenceService {
    CompetenceResponseDTO ajouterCompetence(CompetenceRequestDTO dto);
    List<CompetenceResponseDTO> getAllCompetences();
    CompetenceResponseDTO getCompetenceById(Long id);
    CompetenceResponseDTO updateCompetence(Long id, CompetenceRequestDTO dto);
    void deleteCompetence(Long id);
}

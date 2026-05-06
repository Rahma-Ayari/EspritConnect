package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.EtudiantRequestDTO;
import tn.esprit.espritconnect2.DTO.EtudiantResponseDTO;

import java.util.List;

public interface IEtudiantService {
    EtudiantResponseDTO ajouterEtudiant(EtudiantRequestDTO dto);

    List<EtudiantResponseDTO> getAllEtudiants();

    EtudiantResponseDTO getEtudiantById(Long id);

    EtudiantResponseDTO updateEtudiant(Long id, EtudiantRequestDTO dto);

    void deleteEtudiant(Long id);
}

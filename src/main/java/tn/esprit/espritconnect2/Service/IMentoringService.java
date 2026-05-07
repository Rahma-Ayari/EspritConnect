package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.MentoringRequestDTO;
import tn.esprit.espritconnect2.DTO.MentoringResponseDTO;

import java.util.List;

public interface IMentoringService {

    MentoringResponseDTO ajouterMentoring(MentoringRequestDTO dto);

    List<MentoringResponseDTO> getAllMentorings();

    MentoringResponseDTO getMentoringById(Long id);

    void deleteMentoring(Long id);
}
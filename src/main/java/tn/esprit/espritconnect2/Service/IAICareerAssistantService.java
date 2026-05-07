package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.AICareerAssistantRequestDTO;
import tn.esprit.espritconnect2.DTO.AICareerAssistantResponseDTO;
import tn.esprit.espritconnect2.Entitie.*;

import java.util.List;

public interface IAICareerAssistantService {

    // ================= CRUD =================

    AICareerAssistantResponseDTO ajouterAssistant(
            AICareerAssistantRequestDTO dto
    );

    List<AICareerAssistantResponseDTO> getAllAssistants();

    AICareerAssistantResponseDTO getAssistantById(Long id);

    AICareerAssistantResponseDTO updateAssistant(
            Long id,
            AICareerAssistantRequestDTO dto
    );

    void deleteAssistant(Long id);

    // ================= AI FEATURES =================

    Matching calculerMatchingJob(
            Long etudiantId,
            Long offreId
    );

    List<Alumni> recommanderMentors(
            Long etudiantId
    );

    List<String> analyserCV(
            Fichier fichier
    );

    List<String> analyserProfil(
            Profil profil
    );

    List<Etudiant> topCandidats();
}
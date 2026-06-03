package tn.esprit.espritconnect2.Service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.EvenementRequestDTO;
import tn.esprit.espritconnect2.DTO.EvenementResponseDTO;
import tn.esprit.espritconnect2.DTO.EvenementStatsDTO;
import tn.esprit.espritconnect2.DTO.ParticipationDTO;
import tn.esprit.espritconnect2.Entitie.User;

import java.util.List;

public interface IEvenementService {
    EvenementResponseDTO createEvenement(EvenementRequestDTO dto, User currentUser);
    List<EvenementResponseDTO> getAllEvenements(String search, String status, String type, User currentUser);
    List<EvenementResponseDTO> getPublicEvenements(String search, String type, User currentUser);
    List<EvenementResponseDTO> getUpcomingEvenements(User currentUser);
    List<EvenementResponseDTO> getMyCreatedEvenements(User currentUser);
    EvenementResponseDTO getEvenementById(Long id, User currentUser);
    EvenementResponseDTO getPublicEvenementById(Long id, User currentUser);
    EvenementResponseDTO updateEvenement(Long id, EvenementRequestDTO dto, User currentUser);
    void deleteEvenement(Long id, User currentUser);
    ParticipationDTO participate(Long eventId, User currentUser);
    void cancelParticipation(Long eventId, User currentUser);
    List<ParticipationDTO> getMyParticipations(User currentUser);
    String uploadImage(MultipartFile file);
    Long countEvents();
    EvenementStatsDTO getStats();
}

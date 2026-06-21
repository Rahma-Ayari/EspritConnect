package tn.esprit.espritconnect2.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.DeleteEventEmailRequest;
import tn.esprit.espritconnect2.DTO.EventSuggestionsDTO;
import tn.esprit.espritconnect2.DTO.EvenementRequestDTO;
import tn.esprit.espritconnect2.DTO.EvenementResponseDTO;
import tn.esprit.espritconnect2.DTO.EvenementStatsDTO;
import tn.esprit.espritconnect2.DTO.ParticipationDTO;
import tn.esprit.espritconnect2.DTO.WaitingListDTO;
import tn.esprit.espritconnect2.Entitie.User;

import java.util.List;

public interface IEvenementService {
    EvenementResponseDTO createEvenement(EvenementRequestDTO dto, User currentUser);
    List<EvenementResponseDTO> getAllEvenements(String search, String status, String type, User currentUser);
    Page<EvenementResponseDTO> getAllEvenementsPaged(String search, String status, String type, User currentUser, Pageable pageable);
    List<EvenementResponseDTO> getPublicEvenements(String search, String type, User currentUser);
    List<EvenementResponseDTO> getUpcomingEvenements(User currentUser);
    List<EvenementResponseDTO> getMyCreatedEvenements(User currentUser);
    EvenementResponseDTO getEvenementById(Long id, User currentUser);
    List<ParticipationDTO> getEventParticipations(Long eventId, User currentUser);
    EvenementResponseDTO getPublicEvenementById(Long id, User currentUser);
    EvenementResponseDTO updateEvenement(Long id, EvenementRequestDTO dto, User currentUser);
    void deleteEvenement(Long id, User currentUser, DeleteEventEmailRequest emailRequest);
    void deleteEvenement(Long id, User currentUser);
    ParticipationDTO participate(Long eventId, User currentUser);
    void cancelParticipation(Long eventId, User currentUser);
    WaitingListDTO joinWaitingList(Long eventId, User currentUser);
    void removeFromWaitingList(Long waitingListId, User currentUser);
    List<WaitingListDTO> getEventWaitingList(Long eventId, User currentUser);
    void acceptWaitingListUser(Long waitingListId, User currentUser);
    void rejectWaitingListUser(Long waitingListId, User currentUser);
    ParticipationDTO approveParticipation(Long participationId, User currentUser);
    void rejectParticipation(Long participationId, User currentUser);
    List<EvenementResponseDTO> getArchivedEvenements(User currentUser);
    EventSuggestionsDTO getEventSuggestions(User currentUser);
    List<ParticipationDTO> getMyParticipations(User currentUser);
    String uploadImage(MultipartFile file);
    Long countEvents();
    EvenementStatsDTO getStats();
}

package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.NotificationRequestDTO;
import tn.esprit.espritconnect2.DTO.NotificationResponseDTO;
import tn.esprit.espritconnect2.Entitie.Notification;
import tn.esprit.espritconnect2.Repository.NotificationRepository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;

    private Notification toEntity(NotificationRequestDTO dto) {
        Notification n = new Notification();
        n.setType(dto.getType());
        n.setContenu(dto.getContenu());
        n.setDestinataire(dto.getDestinataire());
        n.setDateEnvoi(new Date());
        n.setLue(false);
        return n;
    }

    private NotificationResponseDTO toDTO(Notification n) {
        return NotificationResponseDTO.builder()
                .idNotification(n.getIdNotification())
                .type(n.getType())
                .contenu(n.getContenu())
                .dateEnvoi(n.getDateEnvoi())
                .lue(n.getLue())
                .destinataire(n.getDestinataire())
                .build();
    }

    @Override
    public NotificationResponseDTO ajouterNotification(NotificationRequestDTO dto) {
        Notification n = toEntity(dto);
        return toDTO(notificationRepository.save(n));
    }

    @Override
    public List<NotificationResponseDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponseDTO getNotificationById(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'id : " + id));
        return toDTO(n);
    }

    @Override
    public List<NotificationResponseDTO> getNotificationsByDestinataire(String destinataire) {
        return notificationRepository.findTop50ByDestinataireOrderByDateEnvoiDesc(destinataire).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countUnreadByDestinataire(String destinataire) {
        return notificationRepository.countByDestinataireAndLueFalse(destinataire);
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'id : " + id));
        n.setLue(true);
        return toDTO(notificationRepository.save(n));
    }

    @Override
    @Transactional
    public void markAllAsReadByDestinataire(String destinataire) {
        List<Notification> notifications = notificationRepository.findByDestinataireOrderByDateEnvoiDesc(destinataire);
        notifications.forEach(n -> n.setLue(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public NotificationResponseDTO updateNotification(Long id, NotificationRequestDTO dto) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'id : " + id));
        n.setType(dto.getType());
        n.setContenu(dto.getContenu());
        n.setDestinataire(dto.getDestinataire());
        return toDTO(notificationRepository.save(n));
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}

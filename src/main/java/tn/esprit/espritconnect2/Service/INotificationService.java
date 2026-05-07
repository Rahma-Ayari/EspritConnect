package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.NotificationRequestDTO;
import tn.esprit.espritconnect2.DTO.NotificationResponseDTO;
import java.util.List;

public interface INotificationService {
    NotificationResponseDTO ajouterNotification(NotificationRequestDTO dto);
    List<NotificationResponseDTO> getAllNotifications();
    NotificationResponseDTO getNotificationById(Long id);
    NotificationResponseDTO updateNotification(Long id, NotificationRequestDTO dto);
    void deleteNotification(Long id);
}

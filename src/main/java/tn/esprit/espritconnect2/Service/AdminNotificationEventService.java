package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.Entitie.Notification;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.NotificationRepository;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AdminNotificationEventService {

    private static final String ADMIN_DESTINATAIRE = "ADMIN";
    private final NotificationRepository notificationRepository;

    public void notifyNewUserRegistration(User user) {
        if (user == null || user.getRole() == Role.ADMIN) {
            return;
        }
        createAdminNotification(
                "USER_CREATED",
                "Nouvelle inscription: " + safe(user.getNom()) + " (" + safe(user.getRole().name()) + ") - " + safe(user.getEmail())
        );
    }

    public void notifyProfileUpdated(User user) {
        if (user == null || user.getRole() == Role.ADMIN) {
            return;
        }
        createAdminNotification(
                "PROFILE_UPDATED",
                "Profil mis a jour: " + safe(user.getNom()) + " (" + safe(user.getRole().name()) + ")"
        );
    }

    private void createAdminNotification(String type, String contenu) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setContenu(contenu);
        notification.setDestinataire(ADMIN_DESTINATAIRE);
        notification.setDateEnvoi(new Date());
        notification.setLue(false);
        notificationRepository.save(notification);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}

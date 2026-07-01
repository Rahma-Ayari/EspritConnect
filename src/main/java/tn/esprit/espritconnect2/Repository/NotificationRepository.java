package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.Notification;
import java.util.Date;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByDateEnvoiBetween(Date startDate, Date endDate);
    long countByLueTrueAndDateEnvoiBetween(Date startDate, Date endDate);
    List<Notification> findByDestinataireOrderByDateEnvoiDesc(String destinataire);
    List<Notification> findTop50ByDestinataireOrderByDateEnvoiDesc(String destinataire);
    long countByDestinataireAndLueFalse(String destinataire);
}

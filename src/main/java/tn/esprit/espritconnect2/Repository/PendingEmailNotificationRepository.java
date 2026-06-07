package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.PendingEmailNotification;
import tn.esprit.espritconnect2.Entitie.Role;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PendingEmailNotificationRepository extends JpaRepository<PendingEmailNotification, Long> {

    List<PendingEmailNotification> findByProcessedFalseOrderByCreatedAtAsc();

    List<PendingEmailNotification> findByProcessedFalseAndPriorityTrueOrderByCreatedAtAsc();

    List<PendingEmailNotification> findByProcessedFalseAndPriorityFalseOrderByCreatedAtAsc();

    @Query("SELECT COUNT(p) FROM PendingEmailNotification p WHERE p.processed = false")
    long countPendingNotifications();

    @Query("SELECT COUNT(p) FROM PendingEmailNotification p WHERE p.processed = false AND p.createdAt >= :since")
    long countPendingNotificationsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(p) FROM PendingEmailNotification p WHERE p.createdAt >= :since")
    long countRegistrationsSince(@Param("since") LocalDateTime since);

    List<PendingEmailNotification> findByProcessedFalseAndCreatedAtBefore(LocalDateTime before);

    List<PendingEmailNotification> findByUserRole(Role role);

    @Query("SELECT p FROM PendingEmailNotification p WHERE p.processed = false AND p.userRole = :role")
    List<PendingEmailNotification> findPendingByRole(@Param("role") Role role);

    void deleteByProcessedTrueAndProcessedAtBefore(LocalDateTime before);
}

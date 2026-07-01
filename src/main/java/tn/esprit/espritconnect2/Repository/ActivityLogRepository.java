package tn.esprit.espritconnect2.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ActivityLog;

import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByUserId(UUID userId, Pageable pageable);
    
    // For statistics
    long countByAction(String action);
    
    // Find last login for user
    ActivityLog findFirstByUserIdAndActionOrderByCreatedAtDesc(UUID userId, String action);
    
    // For global statistics
    ActivityLog findFirstByActionOrderByCreatedAtDesc(String action);
    ActivityLog findFirstByActionInOrderByCreatedAtDesc(java.util.List<String> actions);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void deleteByUserId(UUID userId);
}

package tn.esprit.espritconnect2.Repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.BadgeRequest;
import tn.esprit.espritconnect2.Entitie.RequestStatus;

import java.util.List;

public interface BadgeRequestRepository extends JpaRepository<BadgeRequest, Long> {
    List<BadgeRequest> findByStatus(RequestStatus status);
    java.util.Optional<BadgeRequest> findByBadgeIdAndUserId(Long badgeId, UUID userId);
    List<BadgeRequest> findByUserIdAndBadgeIdAndStatusIn(UUID userId, Long badgeId, java.util.Collection<RequestStatus> statuses);
}

package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.UserBadge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    long countByBadgeId(Long badgeId);
    List<UserBadge> findByUserId(UUID userId);
    Optional<UserBadge> findByBadgeIdAndUserId(Long badgeId, UUID userId);
    void deleteByBadgeIdAndUserId(Long badgeId, UUID userId);
}

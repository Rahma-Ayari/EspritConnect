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
    
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM user_badges WHERE user_id = :userId", nativeQuery = true)
    List<UserBadge> findByUserIdNative(@org.springframework.data.repository.query.Param("userId") String userId);

    List<UserBadge> findByUser_Id(UUID userId);
    Optional<UserBadge> findByBadgeIdAndUser_Id(Long badgeId, UUID userId);
    void deleteByBadgeIdAndUser_Id(Long badgeId, UUID userId);
}

package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.Participation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    boolean existsByUserIdAndEvenementIdEvenement(UUID userId, Long evenementId);
    Optional<Participation> findByUserIdAndEvenementIdEvenement(UUID userId, Long evenementId);
    List<Participation> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Participation> findByEvenementIdEvenementOrderByCreatedAtDesc(Long evenementId);
    long countByEvenementIdEvenement(Long evenementId);
    List<Participation> findByEvenementIdEvenementAndStatusOrderByCreatedAtDesc(Long evenementId, String status);
    long countByEvenementIdEvenementAndStatus(Long evenementId, String status);
}

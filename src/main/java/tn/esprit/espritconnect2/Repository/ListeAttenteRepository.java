package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ListeAttente;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListeAttenteRepository extends JpaRepository<ListeAttente, Long> {
    boolean existsByUserIdAndEvenementIdEvenement(java.util.UUID userId, Long evenementId);
    Optional<ListeAttente> findByUserIdAndEvenementIdEvenement(java.util.UUID userId, Long evenementId);
    List<ListeAttente> findByEvenementIdEvenementOrderByCreatedAtDesc(Long evenementId);
    long countByEvenementIdEvenement(Long evenementId);
}

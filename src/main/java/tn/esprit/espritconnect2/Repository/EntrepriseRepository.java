package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.Entreprise;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
    Optional<Entreprise> findByEmail(String email);
    boolean existsByEmail(String email);

    /** En attente : valide n'est pas true (null ou false), et non refusée. */
    @Query("SELECT COUNT(e) FROM Entreprise e WHERE e.inscriptionRefusee = false AND (e.valide IS NULL OR e.valide = false)")
    long countPendingEntreprises();

    @Query("SELECT e FROM Entreprise e WHERE e.inscriptionRefusee = false AND (e.valide IS NULL OR e.valide = false) ORDER BY e.createdAt DESC, e.idEntreprise DESC")
    List<Entreprise> findPendingEntreprises();

    @Query("SELECT COUNT(e) FROM Entreprise e WHERE e.inscriptionRefusee = false AND e.valide = true")
    long countApprovedEntreprises();
}

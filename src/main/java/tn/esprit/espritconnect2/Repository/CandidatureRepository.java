package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.espritconnect2.Entitie.Candidature;

import java.util.List;
import java.util.Optional;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    boolean existsByEtudiantIdEtudiantAndOffreIdOffre(Long etudiantId, Long offreId);

    Optional<Candidature> findByEtudiantIdEtudiantAndOffreIdOffre(Long etudiantId, Long offreId);

    List<Candidature> findByEtudiantIdEtudiant(Long etudiantId);

    List<Candidature> findByOffreIdOffre(Long offreId);

    @Query("SELECT c FROM Candidature c JOIN FETCH c.etudiant WHERE c.offre.idOffre = :offreId")
    List<Candidature> findByOffreIdWithEtudiant(@Param("offreId") Long offreId);

    List<Candidature> findByOffreIdOffreOrderByScoreMatchDesc(Long offreId);
}

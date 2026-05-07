package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Candidature;

import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    boolean existsByEtudiantIdEtudiantAndOffreIdOffre(Long etudiantId, Long offreId);

    List<Candidature> findByEtudiantIdEtudiant(Long etudiantId);

    List<Candidature> findByOffreIdOffre(Long offreId);

    List<Candidature> findByOffreIdOffreOrderByScoreMatchDesc(Long offreId);
}

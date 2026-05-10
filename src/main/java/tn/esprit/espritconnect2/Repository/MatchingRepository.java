package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Matching;

import java.util.List;
import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    List<Matching> findByEtudiantIdEtudiantOrderByScoreCompatibiliteDesc(Long etudiantId);

    List<Matching> findByOffreIdOffreOrderByScoreCompatibiliteDesc(Long offreId);

    Optional<Matching> findByEtudiantIdEtudiantAndOffreIdOffre(Long etudiantId, Long offreId);
}

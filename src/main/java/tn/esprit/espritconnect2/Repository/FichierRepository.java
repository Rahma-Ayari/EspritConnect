package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Fichier;

import java.util.List;
import java.util.Optional;

public interface FichierRepository extends JpaRepository<Fichier, Long> {
    List<Fichier> findByEtudiantIdEtudiant(Long etudiantId);

    Optional<Fichier> findByCandidatureId(Long candidatureId);

    List<Fichier> findByAlumniIdAlumni(Long alumniId);
}

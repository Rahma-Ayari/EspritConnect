package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Etudiant;

import java.util.Optional;

public interface EtudiantRepository extends JpaRepository<Etudiant,Long> {
    // Vérifier si un email existe déjà (utile pour la validation)
    boolean existsByEmail(String email);

    // Trouver par email (utile pour Spring Security plus tard)
    Optional<Etudiant> findByEmail(String email);
}

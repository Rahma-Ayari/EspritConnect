package tn.esprit.espritconnect2.Repository.emailBackOffice;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.DigestConfig;

public interface DigestConfigRepository extends JpaRepository<DigestConfig, Long> {
    // Spring génère AUTOMATIQUEMENT ces méthodes :
    // ✅ findById(Long id)       → chercher par ID
    // ✅ save(DigestConfig c)    → insérer ou modifier
    // ✅ findAll()               → récupérer tout
    // ✅ deleteById(Long id)     → supprimer

    // Pas besoin d'écrire du SQL ! Spring le fait tout seul.
}

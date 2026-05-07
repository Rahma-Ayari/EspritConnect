package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Entreprise;

public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
}

package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.Offre;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {
}

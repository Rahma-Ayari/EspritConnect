package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.Evenement;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {
}

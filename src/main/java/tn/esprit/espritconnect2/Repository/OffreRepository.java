package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.Offre;
import java.util.Date;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {
    long countByDatePublicationBetween(Date startDate, Date endDate);
}

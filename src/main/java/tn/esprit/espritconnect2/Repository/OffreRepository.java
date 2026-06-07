package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.Offre;
import java.util.Date;
import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {
    List<Offre> findTop10ByDatePublicationAfterOrderByDatePublicationDesc(Date after);
    long countByDatePublicationBetween(Date startDate, Date endDate);

    List<Offre> findByEntreprise_IdEntrepriseOrderByDatePublicationDesc(Long entrepriseId);
}

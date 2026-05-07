package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Offre;
import tn.esprit.espritconnect2.Entitie.StatutOffre;
import tn.esprit.espritconnect2.Entitie.Type;

import java.util.List;

public interface OffreRepository extends JpaRepository<Offre, Long> {

    List<Offre> findByTypeOffreAndStatutOffre(Type typeOffre, StatutOffre statutOffre);

    List<Offre> findByStatutOffreOrderByDatePublicationDesc(StatutOffre statutOffre);
}

package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Evenement;
import java.util.Date;
import java.util.List;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findTop10ByDateEvenementAfterOrderByDateEvenementDesc(Date after);
}

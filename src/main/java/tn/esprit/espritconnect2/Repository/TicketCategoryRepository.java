package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.TicketCategory;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {
}

package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.SupportTicket;

import java.util.List;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByCreatorId(UUID creatorId);
    
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM support_ticket WHERE creator_id = :creatorId", nativeQuery = true)
    List<SupportTicket> findByCreatorIdNative(@org.springframework.data.repository.query.Param("creatorId") String creatorId);
}

package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.SupportTicket;

import java.util.List;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByCreatorId(UUID creatorId);

    long countByStatus(tn.esprit.espritconnect2.Entitie.TicketStatus status);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM support_ticket WHERE creator_id = :creatorId", nativeQuery = true)
    List<SupportTicket> findByCreatorIdNative(@org.springframework.data.repository.query.Param("creatorId") String creatorId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT t.status, COUNT(t) FROM SupportTicket t GROUP BY t.status""")
    List<Object[]> countGroupByStatus();

    @org.springframework.data.jpa.repository.Query("""
            SELECT t.priority, COUNT(t) FROM SupportTicket t GROUP BY t.priority""")
    List<Object[]> countGroupByPriority();

    @org.springframework.data.jpa.repository.Query("""
            SELECT COALESCE(c.name, 'Uncategorized'), COUNT(t)
            FROM SupportTicket t LEFT JOIN t.category c
            GROUP BY COALESCE(c.name, 'Uncategorized')""")
    List<Object[]> countGroupByCategory();

    @org.springframework.data.jpa.repository.Query("""
            SELECT COUNT(t) FROM SupportTicket t
            WHERE t.status = :status AND t.resolvedAt >= :since""")
    long countResolvedSince(
            @org.springframework.data.repository.query.Param("status") tn.esprit.espritconnect2.Entitie.TicketStatus status,
            @org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, resolved_at)) FROM support_ticket WHERE resolved_at IS NOT NULL",
            nativeQuery = true)
    Double averageResolutionHours();
}

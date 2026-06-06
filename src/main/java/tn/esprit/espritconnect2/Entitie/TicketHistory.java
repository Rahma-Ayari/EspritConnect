package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private SupportTicket ticket;

    private String eventType; // e.g. CREATED, ASSIGNED, STATUS_CHANGED, ADMIN_REPLY, REOPENED

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime timestamp;

    private String performedBy;
}

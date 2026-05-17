package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private SupportTicket ticket;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean isInternal;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

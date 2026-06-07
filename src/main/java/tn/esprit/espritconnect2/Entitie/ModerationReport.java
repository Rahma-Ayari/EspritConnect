package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "moderation_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModerationContentType contentType;

    /** Reference to reported content (post id, message id, user id, etc.) */
    @Column(nullable = false)
    private String contentRefId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ModerationStatus status = ModerationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ModerationAction actionTaken = ModerationAction.NONE;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "moderator_id")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    private User moderator;

    @Column(columnDefinition = "TEXT")
    private String moderatorNotes;

    private Long linkedTicketId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;
}

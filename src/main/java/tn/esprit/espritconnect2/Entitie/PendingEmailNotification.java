package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pending_email_notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PendingEmailNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 36)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    private UUID userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role userRole;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean processed = false;

    @Column
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationType notificationType = NotificationType.NEW_REGISTRATION;

    @Column(nullable = false)
    @Builder.Default
    private boolean priority = false;

    public enum NotificationType {
        NEW_REGISTRATION,
        BATCH_SUMMARY,
        HOURLY_DIGEST,
        DAILY_DIGEST
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
    }
}

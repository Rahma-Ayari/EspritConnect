package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FAQ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private TicketCategory category;

    @Builder.Default
    @Column(nullable = false)
    private boolean isImportant = false;

    @Builder.Default
    @Column(nullable = false)
    private int viewCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int helpfulCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int notHelpfulCount = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

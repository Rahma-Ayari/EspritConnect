package tn.esprit.espritconnect2.Entitie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "forum_post_match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    @JsonIgnoreProperties({"replies", "tags"})
    private ForumPost post;

    @Column(nullable = false, length = 100)
    private String matchedUserEmail;

    @Column(nullable = false, length = 100)
    private String matchedUserName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role matchedUserRole;

    @Builder.Default
    private int score = 0;

    @Builder.Default
    private boolean notified = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

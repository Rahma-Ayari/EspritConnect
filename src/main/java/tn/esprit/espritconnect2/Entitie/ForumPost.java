package tn.esprit.espritconnect2.Entitie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forum_post")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private ForumCategory category;

    @Column(nullable = false, length = 100)
    private String authorName;

    @Column(nullable = false, length = 100)
    private String authorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role authorRole;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private boolean pinned;

    private boolean reported;

    @Column(length = 255)
    private String reportReason;

    private int viewsCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties("post")
    private List<ForumReply> replies = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

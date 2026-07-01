package tn.esprit.espritconnect2.Entitie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_reply")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ForumReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    @JsonIgnoreProperties("replies")
    private ForumPost post;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(nullable = false, length = 100)
    private String authorName;

    @Column(nullable = false, length = 100)
    private String authorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role authorRole;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private boolean reported;

    @Column(length = 255)
    private String reportReason;

    @Builder.Default
    private int likesCount = 0;

    @ManyToOne(optional = true)
    @JoinColumn(name = "parent_reply_id")
    @JsonIgnoreProperties({"childReplies", "post", "parentReply"})
    private ForumReply parentReply;

    @OneToMany(mappedBy = "parentReply", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    @JsonIgnoreProperties({"parentReply", "post"})
    private java.util.List<ForumReply> childReplies = new java.util.ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

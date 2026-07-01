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
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @ManyToOne(optional = true)
    @JoinColumn(name = "category_id")
    private ForumCategory category;

    @Column(nullable = false, length = 100)
    private String authorName;

    @Column(nullable = false, length = 100)
    private String authorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role authorRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.PUBLISHED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PostType postType = PostType.QUESTION;

    @ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
    @CollectionTable(name = "forum_post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(length = 500)
    private String website;

    @Column(length = 500)
    @com.fasterxml.jackson.annotation.JsonProperty("coverImageUrl")
    private String coverImageUrl;

    @Column(length = 500)
    private String videoUrl;

    @Column(length = 500)
    private String pdfUrl;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String pdfExtractedText;

    @Column(length = 500)
    private String rejectionReason;

    @Builder.Default
    private boolean allowMentions = true;

    @Builder.Default
    private boolean aiGenerated = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    private boolean pinned;

    private boolean reported;

    @Column(length = 255)
    private String reportReason;

    private int viewsCount;

    @Builder.Default
    private int likesCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    @Builder.Default
    @OrderBy("createdAt ASC")
    @JsonIgnoreProperties("post")
    private List<ForumReply> replies = new ArrayList<>();

    @ManyToOne(optional = true)
    @JoinColumn(name = "discussion_id")
    private ForumDiscussion forumDiscussion;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == PostStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

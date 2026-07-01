package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "forum_discussion")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ForumDiscussion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(optional = true)
    @JoinColumn(name = "category_id")
    private ForumCategory category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "forum_discussion_tags", joinColumns = @JoinColumn(name = "discussion_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String creatorEmail;

    @Column(nullable = false, length = 100)
    private String creatorName;

    @Column(length = 30)
    private String creatorRole;

    @Column(length = 200)
    private String creatorRoleLabel;

    private boolean isPrivate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiscussionStatus status;

    @OneToMany(mappedBy = "forumDiscussion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private java.util.List<ForumPost> posts = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private java.util.List<ForumDiscussionMember> members = new java.util.ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 100)
    private String pendingName;

    @Column(length = 1000)
    private String pendingDescription;

    private Boolean pendingIsPrivate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

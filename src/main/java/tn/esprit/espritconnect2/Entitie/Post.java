package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=200)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private String imageUrl;

    @Column(nullable=false)
    private String category; // "FEED" ou "BUSINESS_DIRECTORY"

    @Column(nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

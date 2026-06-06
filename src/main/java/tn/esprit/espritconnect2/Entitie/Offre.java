package tn.esprit.espritconnect2.Entitie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre")
    private Long idOffre;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Le type d'offre est obligatoire")
    @Enumerated(EnumType.STRING)
    private Type typeOffre; // stage, emploi

    private String localisation;

    private String domaine;

    // New fields for enhanced job management
    private String department;
    
    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;
    
    private Integer numberOfPositions;
    
    @Enumerated(EnumType.STRING)
    private WorkMode workMode;
    
    private Double salaryMin;
    private Double salaryMax;
    private String duration;
    
    @Temporal(TemporalType.DATE)
    private Date deadline;
    
    @ElementCollection
    @CollectionTable(name = "offre_technologies", joinColumns = @JoinColumn(name = "offre_id"))
    @Column(name = "technology")
    private List<String> technologies = new java.util.ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "offre_languages", joinColumns = @JoinColumn(name = "offre_id"))
    @Column(name = "language")
    private List<String> languages = new java.util.ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String responsibilities;
    
    @Column(columnDefinition = "TEXT")
    private String requirements;
    
    @Column(columnDefinition = "TEXT")
    private String benefits;
    
    private Boolean isPinned = false;

    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @ElementCollection
    @CollectionTable(name = "offre_competences", joinColumns = @JoinColumn(name = "offre_id"))
    @Column(name = "competence")
    private List<String> competencesRequises = new java.util.ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_ofrre", length = 50)
    private Status statutOfrre;

    @Temporal(TemporalType.TIMESTAMP)
    private Date datePublication;

    @ManyToOne
    private Entreprise entreprise;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Candidature> candidatures;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Matching> matchings;
}

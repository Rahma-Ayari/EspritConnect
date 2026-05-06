package tn.esprit.espritconnect2.Entitie;


import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "matching")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Matching {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_matching")
    private Long idMatching;

    private Float scoreCompatibilite;
    private String typeMatching;
    private Date dateCalcul;

    @ElementCollection
    @CollectionTable(name = "matching_competences_requises",
            joinColumns = @JoinColumn(name = "matching_id"))
    @Column(name = "competence")
    private List<String> competencesRequises;

    @ElementCollection
    @CollectionTable(name = "matching_recommandations",
            joinColumns = @JoinColumn(name = "matching_id"))
    @Column(name = "recommandation")
    private List<String> recommandations;

    @ManyToOne
    private Offre offre;

    @ManyToOne
    private Etudiant etudiant;
}

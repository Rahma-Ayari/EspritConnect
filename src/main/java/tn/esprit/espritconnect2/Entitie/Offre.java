package tn.esprit.espritconnect2.Entitie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_offre")
    private Long idOffre;
    private String titre;
    private String description;
    @Enumerated(EnumType.STRING)
    private Type typeOffre; // stage, emploi
    private String localisation;
    @Enumerated(EnumType.STRING)
    private StatutOffre statutOffre;
    private Date datePublication;

    @ManyToOne
    private Entreprise entreprise;

    @OneToMany(mappedBy = "offre")
    @JsonIgnore
    private List<Candidature> candidatures;

    @OneToMany(mappedBy = "offre")
    @JsonIgnore
    private List<Matching> matchings;
}

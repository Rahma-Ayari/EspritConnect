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
@Table(name = "etudiant")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etudiant")
    private Long idEtudiant;

    private String nom;
    private String email;
    private String password;
    private Niveau niveau;
    private String filiere;
    private Integer scoreReadiness;
    private Date dateInscription;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profil_id")
    private Profil profil;

    @ManyToMany
    @JsonIgnore
    private List<Competence> competences;

    @OneToMany(mappedBy = "etudiant")
    @JsonIgnore
    private List<Candidature> candidatures;

    @OneToMany(mappedBy = "etudiant")
    @JsonIgnore
    private List<Fichier> fichiers ;

    @OneToMany(mappedBy = "etudiant")
    @JsonIgnore
    private List<Mentoring> mentorings;

    @OneToMany(mappedBy = "etudiant")
    @JsonIgnore
    private List<Matching> matchings;


}

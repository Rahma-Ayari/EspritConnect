package tn.esprit.espritconnect2.Entitie;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "competence")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Competence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_competence")
    private Long idCompetence;

    private String libelle;
    private String categorie;
    private String niveau;

    @ManyToMany(mappedBy = "competences")
    private List<Etudiant> etudiants;

    @ManyToMany(mappedBy = "competences")
    private List<Alumni> alumni;
}
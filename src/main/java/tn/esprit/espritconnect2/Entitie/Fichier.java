package tn.esprit.espritconnect2.Entitie;
//à vérifier si on va le renommer en CV

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fichier")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Fichier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fichier")
    private Long idFichier;

    private String userId;
    private String nom;
    private String url;
    private Type typeFichier;   // "CV" ou "PORTFOLIO"
    private Long taille;

    @ManyToOne
    @JsonIgnore
    private Etudiant etudiant;

    @OneToOne
    @JsonIgnore
    private Candidature candidature;

    @OneToOne
    @JsonIgnore
    private Alumni alumni;
}

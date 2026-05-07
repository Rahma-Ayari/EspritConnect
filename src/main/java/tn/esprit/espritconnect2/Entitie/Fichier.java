package tn.esprit.espritconnect2.Entitie;
//à vérifier si on va le renommer en CV

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
    @Enumerated(EnumType.STRING)
    private Type typeFichier;   // "CV" ou "PORTFOLIO"
    private Long taille;

    @ManyToOne
    private Etudiant etudiant;

    @OneToOne
    private Candidature candidature;

    @OneToOne
    private Alumni alumni;
}

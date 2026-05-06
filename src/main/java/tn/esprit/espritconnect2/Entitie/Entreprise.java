package tn.esprit.espritconnect2.Entitie;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "entreprise")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entreprise")
    private Long idEntreprise;

    private String nom;
    private String email;
    private String password;
    private String secteur;
    private String siteWeb;
    private Boolean valide;
    private String description;

    @OneToOne
    @JoinColumn(name = "profil_id")
    private Profil profil;

    @OneToMany(mappedBy = "entreprise")
    private List<Offre> offres;

    @OneToMany(mappedBy = "entreprise")
    private List<Evenement> evenements;
}

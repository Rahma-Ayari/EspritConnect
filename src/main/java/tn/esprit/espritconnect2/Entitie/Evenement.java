package tn.esprit.espritconnect2.Entitie;


import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "evenement")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evenement")
    private Long idEvenement;

    private String organisateurId; //à vérifier
    private String titre;
    private String lieu;
    private Date dateEvenement;
    private Integer capacite;
    private String type;

    @ManyToOne
    private Entreprise entreprise;
}

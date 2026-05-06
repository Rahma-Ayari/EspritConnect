package tn.esprit.espritconnect2.Entitie;


import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "notification")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Long idNotification;

    private String type;
    private String contenu;
    private Date dateEnvoi;
    private Boolean lue;
    private String destinataire;

    @ManyToOne
    private Profil profil;
}

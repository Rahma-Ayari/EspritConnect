package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "message")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    private Long idMessage;

    private String userId;
    private Date dateEnvoi;
    private Boolean lu;
    private String expediteur;
    private String contenu;

    @ManyToOne
    private Profil profil;
}

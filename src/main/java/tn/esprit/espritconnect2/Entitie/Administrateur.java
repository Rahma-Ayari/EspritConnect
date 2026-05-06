package tn.esprit.espritconnect2.Entitie;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "administrateur")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Administrateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_admin")
    private Long idAdmin;

    private String nom;
    private String email;
    private String role;
    private String password;

    @OneToOne
    private Profil profil;
}

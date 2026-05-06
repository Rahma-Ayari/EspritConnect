package tn.esprit.espritconnect2.Entitie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "profil")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profil")
    private Long idProfil;

    private String userId;
    private String photo;
    private String lienLinkedIn;
    private String bio;
    private String lienGitHub;

    @OneToMany(mappedBy = "profil")
    @JsonIgnore
    private List<Message> messages;

    @OneToMany(mappedBy = "profil")
    @JsonIgnore
    private List<Notification> notifications;
    @OneToOne(mappedBy = "profil")
    private Etudiant etudiant;

    @OneToOne(mappedBy = "profil")
    private Alumni alumni;

    @OneToOne(mappedBy = "profil")
    private Entreprise entreprise;

    @ManyToOne
    private AICareerAssistant aiCareerAssistant;

    @OneToOne(mappedBy = "profil")
    private Administrateur administrateur;
}

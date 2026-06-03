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
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String photo;
    private String lienLinkedIn;
    private String bio;
    private String lienGitHub;

    // Champs additionnels pour un profil complet
    private String prenom;
    private String telephone;
    private String adresse;
    private String ville;
    private String pays;
    private String codePostal;
    private String siteWeb;
    private String dateNaissance;
    private String genre;
    private String nomProprietaire;

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

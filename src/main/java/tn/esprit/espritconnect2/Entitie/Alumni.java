package tn.esprit.espritconnect2.Entitie;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "alumni")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Alumni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alumni")
    private Long idAlumni;

    private String nom;
    private String email;
    private String password;
    private Integer anneePromotion;
    private String domaine;
    private Boolean disponibleMentorat;
    private String entrepriseActuelle;

    @OneToOne
    private Profil profil;

    @ManyToMany
    @JsonIgnore
    private List<Competence> competences ;

    @OneToMany(mappedBy = "alumni")
    @JsonIgnore
    private List<Mentoring> mentorings ;

//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "ai_assistant_id", referencedColumnName = "id_assistant")
//    private AICareerAssistant aiCareerAssistant;
}

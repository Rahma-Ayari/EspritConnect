package tn.esprit.espritconnect2.Entitie;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Mentoring {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Mentoring")
    private Long idMentoring;
    private Date dateDebut;
    private Date dateFin;
    @Enumerated(EnumType.STRING)
    private Status statutMentoring;
    private String domaine;
    private String objectifs;

    @ManyToOne
    private Etudiant etudiant;

    @ManyToOne
    private Alumni alumni;
}

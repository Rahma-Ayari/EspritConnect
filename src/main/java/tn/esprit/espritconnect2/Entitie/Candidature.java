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
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Candidature")
    private Long id;
    private Date dateCandidature;
    @Enumerated(EnumType.STRING)
    private Status statutCandidature;
    private String lettreMotivation;
    private Float scoreMatch;

    @ManyToOne
    private Etudiant etudiant;

    @ManyToOne
    private Offre offre;

    @OneToOne(mappedBy = "candidature")
    private Fichier fichier;
}

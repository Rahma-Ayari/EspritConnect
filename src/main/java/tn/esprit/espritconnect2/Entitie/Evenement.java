package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "evenement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evenement")
    private Long idEvenement;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "Le lieu est obligatoire")
    private String lieu;

    @NotNull(message = "La date de l'evenement est obligatoire")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEvenement;

    @NotNull(message = "La date de debut est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    @NotNull(message = "L'heure de debut est obligatoire")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heureFin;

    private Integer dureeMinutes;

    @Positive(message = "La capacite doit etre positive")
    private Integer capacite;

    @Column(nullable = false)
    private Boolean unlimitedParticipants = false;

    @Column(nullable = false)
    private Integer nombreParticipants = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_evenement_id")
    private TypeEvenement typeEvenement;

    private String type;

    @Column(length = 1024)
    private String imageUrl;

    @Column(length = 30)
    private String status;

    @ManyToOne
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participations;
}

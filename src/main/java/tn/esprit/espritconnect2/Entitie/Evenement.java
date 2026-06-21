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
    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "lieu")
    private String lieu;

    @NotNull(message = "La date de l'evenement est obligatoire")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_evenement")
    private Date dateEvenement;

    @NotNull(message = "La date de debut est obligatoire")
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @NotNull(message = "L'heure de debut est obligatoire")
    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Column(name = "duree_minutes")
    private Integer dureeMinutes;

    @Positive(message = "La capacite doit etre positive")
    @Column(name = "capacite")
    private Integer capacite;

    @Column(name = "unlimited_participants", nullable = false)
    private Boolean unlimitedParticipants = false;

    @Column(name = "nombre_participants", nullable = false)
    private Integer nombreParticipants = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_evenement_id")
    private TypeEvenement typeEvenement;

    @Column(name = "type_evenement")
    private String type;

    @Column(length = 1024, name = "image_url")
    private String imageUrl;

    @Column(length = 30, name = "status")
    private String status;

    @Column(nullable = true, name = "online")
    private Boolean online = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participations;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}

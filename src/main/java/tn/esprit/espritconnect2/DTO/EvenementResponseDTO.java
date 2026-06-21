package tn.esprit.espritconnect2.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvenementResponseDTO {
    private Long idEvenement;
    private String titre;
    private String lieu;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateEvenement;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFin;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime heureDebut;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime heureFin;

    private Integer dureeMinutes;
    private Integer capacite;
    private Boolean unlimitedParticipants;
    private Boolean online;
    private Integer nombreParticipants;
    private Integer placesRestantes;
    private Long typeEvenementId;
    private String type;
    private String imageUrl;
    private String status;
    private String entrepriseNom;
    private Long entrepriseId;
    private UUID ownerId;
    private String ownerNom;
    private Boolean participated;
    private Boolean ownedByCurrentUser;
    private Double latitude;
    private Double longitude;
}

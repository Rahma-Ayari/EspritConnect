package tn.esprit.espritconnect2.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvenementRequestDTO {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String lieu;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateEvenement;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "La date de debut est obligatoire")
    private LocalDate dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "L'heure de debut est obligatoire")
    private LocalTime heureDebut;

    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heureFin;

    @Positive(message = "La capacite doit etre positive")
    private Integer capacite;

    private Boolean unlimitedParticipants;
    private Boolean online;
    private Long typeEvenementId;
    private String imageUrl;
    private String status;
    private Long entrepriseId;
    private Double latitude;
    private Double longitude;
}

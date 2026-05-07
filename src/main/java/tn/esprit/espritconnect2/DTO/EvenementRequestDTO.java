package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.Date;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class EvenementRequestDTO {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "Le lieu est obligatoire")
    private String lieu;

    @NotNull(message = "La date de l'événement est obligatoire")
    private Date dateEvenement;

    @Positive(message = "La capacité doit être positive")
    private Integer capacite;

    private String type;

    @NotNull(message = "L'ID de l'entreprise est obligatoire")
    private Long entrepriseId;
}

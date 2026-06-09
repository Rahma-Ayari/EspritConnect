package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypeEvenementDTO {
    private Long idTypeEvenement;

    @NotBlank(message = "Le nom du type est obligatoire")
    private String nom;

    private String description;
    private Boolean actif;
}

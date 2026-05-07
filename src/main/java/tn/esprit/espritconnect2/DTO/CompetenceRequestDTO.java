package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CompetenceRequestDTO {
    @NotBlank(message = "Le libellé est obligatoire")
    private String libelle;
    private String categorie;
    private String niveau;
}

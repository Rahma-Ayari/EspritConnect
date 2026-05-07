package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CompetenceResponseDTO {
    private Long idCompetence;
    private String libelle;
    private String categorie;
    private String niveau;
}

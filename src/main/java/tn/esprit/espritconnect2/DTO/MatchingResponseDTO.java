package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingResponseDTO {
    private Long idMatching;
    private Float scoreCompatibilite;
    private String typeMatching;
    private Date dateCalcul;
    private List<String> competencesRequises;
    private List<String> recommandations;
    private Long etudiantId;
    private Long offreId;
}

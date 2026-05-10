package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.espritconnect2.Entitie.Status;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureResponseDTO {
    private Long id;
    private Date dateCandidature;
    private Status statutCandidature;
    private String lettreMotivation;
    private Float scoreMatch;
    private Long etudiantId;
    private Long offreId;
    private Long fichierId;
}

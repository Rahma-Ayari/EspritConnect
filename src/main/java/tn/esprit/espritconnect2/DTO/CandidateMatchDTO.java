package tn.esprit.espritconnect2.DTO;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.Status;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateMatchDTO {
    private Long candidatureId;
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantEmail;
    private String filiere;
    private String niveau;
    private Float scoreCompatibilite;
    private Integer skillsScore;
    private Integer experienceScore;
    private Integer educationScore;
    private List<String> skillsMatched;
    private List<String> recommandations;
    private String lettreMotivationExcerpt;
    private boolean hasResume;
    private Status candidatureStatus;
}

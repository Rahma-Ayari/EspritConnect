package tn.esprit.espritconnect2.DTO;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.Status;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentoringRequestDTO {

    private Date dateDebut;

    private Date dateFin;

    private Status statutMentoring;

    private String domaine;

    private String objectifs;

    private Long etudiantId;

    private Long alumniId;
}
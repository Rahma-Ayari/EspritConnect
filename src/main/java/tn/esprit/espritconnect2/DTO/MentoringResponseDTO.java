package tn.esprit.espritconnect2.DTO;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.Status;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringResponseDTO {

    private Long idMentoring;

    private Date dateDebut;

    private Date dateFin;

    private Status statutMentoring;

    private String domaine;

    private String objectifs;

    private String nomEtudiant;

    private String nomAlumni;
}
package tn.esprit.espritconnect2.DTO;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Entitie.Type;

import java.util.Date;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class OffreResponseDTO {
    private Long idOffre;
    private String titre;
    private String description;
    private Type typeOffre;
    private String localisation;
    private Status statutOfrre;
    private Date datePublication;
    private String entrepriseNom;
    private Long entrepriseId;
}

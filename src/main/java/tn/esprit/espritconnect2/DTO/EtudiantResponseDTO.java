package tn.esprit.espritconnect2.DTO;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.Niveau;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtudiantResponseDTO {

    private Long idEtudiant;
    private String nom;
    private String email;
    private Niveau niveau;
    private String filiere;
    private Integer scoreReadiness;
    private Date dateInscription;

}

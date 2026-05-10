package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.espritconnect2.Entitie.Type;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichierResponseDTO {
    private Long idFichier;
    private String nom;
    private String url;
    private Type typeFichier;
    private Long taille;
    private Long etudiantId;
    private Long alumniId;
    private Long candidatureId;
}

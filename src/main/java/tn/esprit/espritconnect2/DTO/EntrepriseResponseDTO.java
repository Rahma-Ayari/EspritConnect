package tn.esprit.espritconnect2.DTO;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class EntrepriseResponseDTO {
    private Long idEntreprise;
    private String nom;
    private String email;
    private String secteur;
    private String siteWeb;
    private Boolean valide;
    private String description;
}

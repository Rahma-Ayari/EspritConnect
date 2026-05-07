package tn.esprit.espritconnect2.DTO;

import lombok.*;

import java.util.Date;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class EvenementResponseDTO {
    private Long idEvenement;
    private String titre;
    private String lieu;
    private Date dateEvenement;
    private Integer capacite;
    private String type;
    private String entrepriseNom;
    private Long entrepriseId;
}

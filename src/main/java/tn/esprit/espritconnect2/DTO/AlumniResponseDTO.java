package tn.esprit.espritconnect2.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniResponseDTO {

    private Long idAlumni;

    private String nom;

    private String email;

    private Integer anneePromotion;

    private String domaine;

    private Boolean disponibleMentorat;

    private String entrepriseActuelle;
}
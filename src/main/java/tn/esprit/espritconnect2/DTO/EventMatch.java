package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMatch {
    private Long idEvenement;
    private String titre;
    private String status;
}

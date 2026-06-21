package tn.esprit.espritconnect2.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaitingListDTO {
    private Long idListeAttente;
    private UUID userId;
    private String userNom;
    private String userEmail;
    private Long evenementId;
    private String evenementTitre;
    private LocalDateTime createdAt;
}

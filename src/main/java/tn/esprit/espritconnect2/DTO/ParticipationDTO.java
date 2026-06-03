package tn.esprit.espritconnect2.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipationDTO {
    private Long idParticipation;
    private UUID userId;
    private Long evenementId;
    private LocalDateTime createdAt;
    private EvenementResponseDTO evenement;
}

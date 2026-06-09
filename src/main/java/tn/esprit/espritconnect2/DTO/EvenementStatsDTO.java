package tn.esprit.espritconnect2.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvenementStatsDTO {
    private Long totalEvents;
    private Long activeEvents;
    private Long upcomingEvents;
    private Long cancelledEvents;
    private Long completedEvents;
    private Long totalCapacity;
    private Long totalParticipants;
    private Double participationRate;
}

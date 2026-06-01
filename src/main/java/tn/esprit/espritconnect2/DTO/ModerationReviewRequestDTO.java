package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import tn.esprit.espritconnect2.Entitie.ModerationAction;
import tn.esprit.espritconnect2.Entitie.ModerationStatus;

@Data
public class ModerationReviewRequestDTO {
    private ModerationStatus status;
    private ModerationAction actionTaken;
    private String moderatorNotes;
    private Long linkedTicketId;
}

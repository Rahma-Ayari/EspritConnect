package tn.esprit.espritconnect2.DTO;

import lombok.Builder;
import lombok.Data;
import tn.esprit.espritconnect2.Entitie.ModerationAction;
import tn.esprit.espritconnect2.Entitie.ModerationContentType;
import tn.esprit.espritconnect2.Entitie.ModerationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ModerationReportDTO {
    private Long id;
    private ModerationContentType contentType;
    private String contentRefId;
    private String reason;
    private ModerationStatus status;
    private ModerationAction actionTaken;
    private UUID reporterId;
    private String reporterName;
    private UUID moderatorId;
    private String moderatorName;
    private String moderatorNotes;
    private Long linkedTicketId;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}

package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import tn.esprit.espritconnect2.Entitie.ModerationContentType;

@Data
public class ModerationReportRequestDTO {
    private ModerationContentType contentType;
    private String contentRefId;
    private String reason;
}

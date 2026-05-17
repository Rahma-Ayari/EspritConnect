package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import java.util.UUID;

@Data
public class BadgeRequestReqDTO {
    private Long badgeId;
    private UUID userId;
    private String motivation;
}

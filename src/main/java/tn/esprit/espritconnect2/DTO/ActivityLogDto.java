package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ActivityLogDto {
    private Long id;
    private UUID userId;
    private String username;
    private String action;
    private String entity;
    private String entityId;
    private String description;
    private String ipAddress;
    private String browser;
    private LocalDateTime createdAt;
}

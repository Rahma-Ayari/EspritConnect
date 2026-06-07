package tn.esprit.espritconnect2.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlatformMonitoringDTO {
    private String status;
    private LocalDateTime checkedAt;
    private boolean databaseUp;
    private long heapUsedMb;
    private long heapMaxMb;
    private double heapUsagePercent;
    private long activeTickets;
    private long pendingModerationReports;
    private boolean chatbotConfigured;
    private String applicationName;
}

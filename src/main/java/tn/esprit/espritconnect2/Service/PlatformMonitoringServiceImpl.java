package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.PlatformMonitoringDTO;
import tn.esprit.espritconnect2.Entitie.ModerationStatus;
import tn.esprit.espritconnect2.Entitie.TicketStatus;
import tn.esprit.espritconnect2.Repository.ModerationReportRepository;
import tn.esprit.espritconnect2.Repository.SupportTicketRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlatformMonitoringServiceImpl implements IPlatformMonitoringService {

    private final DataSource dataSource;
    private final SupportTicketRepository ticketRepository;
    private final ModerationReportRepository moderationReportRepository;
    private final ChatbotAiService chatbotAiService;

    @Value("${spring.application.name:EspritConnect}")
    private String applicationName;

    @Override
    public PlatformMonitoringDTO getStatus() {
        Runtime runtime = Runtime.getRuntime();
        long used = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long max = runtime.maxMemory() / (1024 * 1024);
        double pct = max > 0 ? (used * 100.0 / max) : 0;

        boolean dbUp = checkDatabase();
        long activeTickets = ticketRepository.countByStatus(TicketStatus.OPEN)
                + ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);

        String status = dbUp && pct < 90 ? "UP" : (dbUp ? "WARNING" : "DOWN");

        return PlatformMonitoringDTO.builder()
                .status(status)
                .checkedAt(LocalDateTime.now())
                .databaseUp(dbUp)
                .heapUsedMb(used)
                .heapMaxMb(max)
                .heapUsagePercent(Math.round(pct * 10.0) / 10.0)
                .activeTickets(activeTickets)
                .pendingModerationReports(moderationReportRepository.countByStatus(ModerationStatus.PENDING))
                .chatbotConfigured(chatbotAiService.isConfigured())
                .applicationName(applicationName)
                .build();
    }

    private boolean checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}

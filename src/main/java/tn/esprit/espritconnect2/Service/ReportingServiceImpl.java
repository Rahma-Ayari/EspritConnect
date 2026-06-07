package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.Entitie.SupportTicket;
import tn.esprit.espritconnect2.Repository.SupportTicketRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements IReportingService {

    private final SupportTicketRepository ticketRepository;


    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public byte[] exportTicketsCsv(LocalDateTime from, LocalDateTime to) {
        List<SupportTicket> tickets = ticketRepository.findAll().stream()
                .filter(t -> isInRange(t.getCreatedAt(), from, to))
                .collect(Collectors.toList());

        StringBuilder csv = new StringBuilder();
        csv.append("id,title,status,priority,category,creator,assignedTo,createdAt,resolvedAt\n");
        for (SupportTicket t : tickets) {
            csv.append(t.getId()).append(',')
                    .append(escape(t.getTitle())).append(',')
                    .append(t.getStatus()).append(',')
                    .append(t.getPriority()).append(',')
                    .append(escape(t.getCategory() != null ? t.getCategory().getName() : "")).append(',')
                    .append(escape(t.getCreator() != null ? t.getCreator().getEmail() : "")).append(',')
                    .append(escape(t.getAssignedTo() != null ? t.getAssignedTo().getEmail() : "")).append(',')
                    .append(t.getCreatedAt() != null ? t.getCreatedAt().format(FMT) : "").append(',')
                    .append(t.getResolvedAt() != null ? t.getResolvedAt().format(FMT) : "").append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }


    private boolean isInRange(LocalDateTime value, LocalDateTime from, LocalDateTime to) {
        if (value == null) {
            return true;
        }
        if (from != null && value.isBefore(from)) {
            return false;
        }
        if (to != null && value.isAfter(to)) {
            return false;
        }
        return true;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

package tn.esprit.espritconnect2.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import tn.esprit.espritconnect2.Entitie.TicketPriority;
import tn.esprit.espritconnect2.Entitie.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketDTO {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private String attachmentUrl;
    private java.util.List<String> tags;
    
    private Long categoryId;
    private String categoryName;
    
    private UUID creatorId;
    private String creatorName;
    
    private UUID assignedToId;
    private String assignedToName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime resolvedAt;

    private String slaMessage;
    private java.util.List<TicketHistoryDTO> timeline;
}

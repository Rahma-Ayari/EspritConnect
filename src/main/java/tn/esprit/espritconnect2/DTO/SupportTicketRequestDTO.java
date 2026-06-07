package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import tn.esprit.espritconnect2.Entitie.TicketPriority;

@Data
public class SupportTicketRequestDTO {
    private String title;
    private String description;
    private Long categoryId;
    private TicketPriority priority;
    private String attachmentUrl;
    private java.util.List<String> tags;
}

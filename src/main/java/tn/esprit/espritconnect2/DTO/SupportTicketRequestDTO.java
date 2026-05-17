package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import tn.esprit.espritconnect2.Entitie.TicketPriority;

@Data
public class SupportTicketRequestDTO {
    private String subject;
    private String description;
    private Long categoryId;
    private TicketPriority priority;
}

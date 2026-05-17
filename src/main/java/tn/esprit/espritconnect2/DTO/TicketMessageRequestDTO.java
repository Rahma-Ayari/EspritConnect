package tn.esprit.espritconnect2.DTO;

import lombok.Data;

@Data
public class TicketMessageRequestDTO {
    private String content;
    private boolean isInternal;
}

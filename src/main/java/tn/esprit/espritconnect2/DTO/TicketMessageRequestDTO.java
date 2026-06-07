package tn.esprit.espritconnect2.DTO;

import lombok.Data;

@Data
public class TicketMessageRequestDTO {
    private String content;
    private boolean isInternal;
    /** Optional URL of an uploaded file/screenshot to attach to this message */
    private String attachmentUrl;
}

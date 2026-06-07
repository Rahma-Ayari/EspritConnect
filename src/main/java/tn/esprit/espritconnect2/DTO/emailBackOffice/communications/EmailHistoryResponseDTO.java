package tn.esprit.espritconnect2.DTO.emailBackOffice.communications;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailDeliveryStatus;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailHistoryType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailHistoryResponseDTO {
    private Long id;
    private EmailHistoryType type;
    private Long campaignId;
    private String toEmail;
    private String subject;
    private EmailDeliveryStatus deliveryStatus;
    private String errorMessage;
    private String sentAt;
}
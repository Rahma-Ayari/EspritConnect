package tn.esprit.espritconnect2.DTO.emailBackOffice.communications;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailCampaignStatus;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.RecipientScope;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCampaignResponseDTO {
    private Long id;
    private String subject;
    private String htmlBody;
    private String fromEmail;
    private EmailCampaignStatus status;
    private RecipientScope recipientScope;
    private Long mailingListId;
    private String createdAt;
    private String updatedAt;
    private String sentAt;
}
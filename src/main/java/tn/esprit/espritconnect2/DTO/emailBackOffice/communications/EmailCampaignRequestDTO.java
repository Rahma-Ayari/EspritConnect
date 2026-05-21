package tn.esprit.espritconnect2.DTO.emailBackOffice.communications;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.RecipientScope;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailCampaignRequestDTO {

    @NotBlank
    @Size(max = 254)
    private String subject;

    @NotBlank
    private String htmlBody;

    @Email
    @NotBlank
    private String fromEmail;

    @NotNull
    private RecipientScope recipientScope;

    /** Obligatoire si recipientScope = MAILING_LIST */
    private Long mailingListId;
}
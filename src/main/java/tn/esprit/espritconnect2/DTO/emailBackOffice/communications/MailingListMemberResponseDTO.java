package tn.esprit.espritconnect2.DTO.emailBackOffice.communications;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailingListMemberResponseDTO {
    private Long id;
    private Long mailingListId;
    private String email;
    private String nom;
    private String userUuid;
}
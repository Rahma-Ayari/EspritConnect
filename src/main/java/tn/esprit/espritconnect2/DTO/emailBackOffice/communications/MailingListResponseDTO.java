package tn.esprit.espritconnect2.DTO.emailBackOffice.communications;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailingListResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String createdAt;
    private long membersCount;
}
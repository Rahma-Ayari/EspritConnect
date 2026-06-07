package tn.esprit.espritconnect2.DTO.emailBackOffice.communications;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailingListMemberRequestDTO {
    @Email
    @NotBlank
    private String email;

    @Size(max = 120)
    private String nom;

    @Size(max = 36)
    private String userUuid;
}
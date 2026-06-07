package tn.esprit.espritconnect2.DTO.emailBackOffice.communications;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailingListRequestDTO {
    @NotBlank
    @Size(max = 150)
    private String name;

    private String description;
}
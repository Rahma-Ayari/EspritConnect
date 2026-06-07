package tn.esprit.espritconnect2.DTO.emailBackOffice.communications;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendTestBirthdayEmailRequestDTO {
    @Email
    @NotBlank
    private String toEmail;

    @NotBlank
    private String nomDemo;
}
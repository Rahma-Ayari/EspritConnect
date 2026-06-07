package tn.esprit.espritconnect2.DTO.emailBackOffice.communications;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BirthdayEmailSettingsRequestDTO {

    @NotNull
    private Boolean active;

    @NotBlank
    @Size(max = 254)
    private String subject;

    @NotBlank
    private String templateHtml;

    @Min(0)
    @Max(23)
    private Integer sendHour;
}
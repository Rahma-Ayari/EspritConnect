package tn.esprit.espritconnect2.DTO.emailBackOffice.communications;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthdayEmailSettingsResponseDTO {
    private Long id;
    private Boolean active;
    private String subject;
    private String templateHtml;
    private Integer sendHour;
    private String lastRunAt;
}
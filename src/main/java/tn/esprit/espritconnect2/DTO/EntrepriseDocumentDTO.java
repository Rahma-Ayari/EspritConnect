package tn.esprit.espritconnect2.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntrepriseDocumentDTO {
    private Long id;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}

package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntrepriseDocumentRequestDTO {
    @NotBlank
    private String documentType;
    @NotBlank
    private String fileName;
    private String fileUrl;
}

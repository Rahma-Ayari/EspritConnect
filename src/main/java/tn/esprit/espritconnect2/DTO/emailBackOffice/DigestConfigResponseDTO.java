package tn.esprit.espritconnect2.DTO.emailBackOffice;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DigestConfigResponseDTO {
    private Long id;
    private String sujet;
    private String bannerUrl;
    private String frequence;
    private Boolean actif;
    private String templateHtml;
    private String frontendBaseUrl;
    private String lastSentAt;
    private DigestSectionsDTO sections;
}
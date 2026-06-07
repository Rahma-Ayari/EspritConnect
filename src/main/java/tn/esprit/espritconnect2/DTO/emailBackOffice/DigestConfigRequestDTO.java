package tn.esprit.espritconnect2.DTO.emailBackOffice;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DigestConfigRequestDTO {
    private String sujet;
    private String bannerUrl;
    private String frequence;       // "DAILY" | "WEEKLY"
    private Boolean actif;
    private String templateHtml;
    private String frontendBaseUrl;
    private Long mailingListId;
    private DigestSectionsDTO sections;
}
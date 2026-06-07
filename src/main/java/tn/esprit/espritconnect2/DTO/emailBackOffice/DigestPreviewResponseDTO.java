package tn.esprit.espritconnect2.DTO.emailBackOffice;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DigestPreviewResponseDTO {
    private String html; // ← Le HTML complet de l'email (pour l'aperçu dans le navigateur)
}
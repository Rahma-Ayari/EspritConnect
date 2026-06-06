package tn.esprit.espritconnect2.DTO;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.VerificationStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntrepriseVerificationDTO {
    private Long entrepriseId;
    private String entrepriseNom;
    private Boolean valide;
    private VerificationStatus verificationStatus;
    private String verificationNotes;
    private long documentsCount;
    private boolean canPostOffers;
    private List<EntrepriseDocumentDTO> documents;
}

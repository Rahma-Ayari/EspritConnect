package tn.esprit.espritconnect2.DTO;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.VerificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnterpriseVerificationDTO {
    private UUID userId;
    private String nom;
    private String email;
    private String businessRegistrationNumber;
    private String companySector;
    private String companyWebsite;
    private String companyDescription;
    private String verificationDocumentName;
    private boolean hasDocument;
    private VerificationStatus verificationStatus;
    private String verificationNotes;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private LocalDateTime createdAt;
}

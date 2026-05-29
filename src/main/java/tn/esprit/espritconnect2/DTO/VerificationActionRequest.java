package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.esprit.espritconnect2.Entitie.VerificationStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationActionRequest {
    @NotNull(message = "Le statut de vérification est requis")
    private VerificationStatus status;
    
    private String notes;
}

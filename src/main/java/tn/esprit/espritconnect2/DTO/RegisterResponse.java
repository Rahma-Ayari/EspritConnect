package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private String message;
    private String email;
    private boolean emailVerificationRequired;
    private String verificationUrl;
}

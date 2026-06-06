package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationSettingsDTO {

    private boolean linkedInEnabled;
    private boolean facebookEnabled;
    private boolean googleEnabled;
    private boolean ssoEnabled;
    private boolean emailEnabled;
    private boolean appleEnabled;

    @Size(max = 500_000)
    private String termsAndPrivacyHtml;
}

package tn.esprit.espritconnect2.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.espritconnect2.DTO.RegistrationSettingsDTO;
import tn.esprit.espritconnect2.Service.RegistrationSettingsService;

/**
 * Public read model for the self-service registration UI (which providers are on, legal copy).
 */
@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationPublicController {

    private final RegistrationSettingsService registrationSettingsService;

    @GetMapping("/settings")
    public ResponseEntity<RegistrationSettingsDTO> getPublicSettings() {
        return ResponseEntity.ok(registrationSettingsService.getSettings());
    }
}

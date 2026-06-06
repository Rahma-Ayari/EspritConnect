package tn.esprit.espritconnect2.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.GeneralSettingsDTO;
import tn.esprit.espritconnect2.DTO.RegionalSettingsDTO;
import tn.esprit.espritconnect2.DTO.RegistrationSettingsDTO;
import tn.esprit.espritconnect2.Service.IPlatformSettingsService;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final IPlatformSettingsService platformSettingsService;

    @GetMapping("/general")
    public ResponseEntity<GeneralSettingsDTO> getGeneral() {
        return ResponseEntity.ok(platformSettingsService.getGeneralSettings());
    }

    @PutMapping("/general")
    public ResponseEntity<GeneralSettingsDTO> updateGeneral(@RequestBody GeneralSettingsDTO dto) {
        return ResponseEntity.ok(platformSettingsService.updateGeneralSettings(dto));
    }

    @GetMapping("/regional")
    public ResponseEntity<RegionalSettingsDTO> getRegional() {
        return ResponseEntity.ok(platformSettingsService.getRegionalSettings());
    }

    @PutMapping("/regional")
    public ResponseEntity<RegionalSettingsDTO> updateRegional(@RequestBody RegionalSettingsDTO dto) {
        return ResponseEntity.ok(platformSettingsService.updateRegionalSettings(dto));
    }

    @GetMapping("/registration")
    public ResponseEntity<RegistrationSettingsDTO> getRegistration() {
        return ResponseEntity.ok(platformSettingsService.getRegistrationSettings());
    }

    @PutMapping("/registration")
    public ResponseEntity<RegistrationSettingsDTO> updateRegistration(@RequestBody RegistrationSettingsDTO dto) {
        return ResponseEntity.ok(platformSettingsService.updateRegistrationSettings(dto));
    }
}

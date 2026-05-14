package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.espritconnect2.DTO.GeneralSettingsDTO;
import tn.esprit.espritconnect2.Service.GeneralSettingsService;

@RestController
@RequestMapping("/api/admin/settings/general")
@RequiredArgsConstructor
public class AdminGeneralSettingsController {

    private final GeneralSettingsService generalSettingsService;

    @GetMapping
    public ResponseEntity<GeneralSettingsDTO> get() {
        return ResponseEntity.ok(generalSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<GeneralSettingsDTO> update(@Valid @RequestBody GeneralSettingsDTO dto) {
        return ResponseEntity.ok(generalSettingsService.updateSettings(dto));
    }
}

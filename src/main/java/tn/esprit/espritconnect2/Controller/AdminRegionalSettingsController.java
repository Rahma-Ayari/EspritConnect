package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.espritconnect2.DTO.RegionalSettingsDTO;
import tn.esprit.espritconnect2.Service.RegionalSettingsService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings/regional")
@RequiredArgsConstructor
public class AdminRegionalSettingsController {

    private final RegionalSettingsService regionalSettingsService;

    @GetMapping
    public ResponseEntity<RegionalSettingsDTO> get() {
        return ResponseEntity.ok(regionalSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<?> update(@Valid @RequestBody RegionalSettingsDTO dto) {
        try {
            return ResponseEntity.ok(regionalSettingsService.updateSettings(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

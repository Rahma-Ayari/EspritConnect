package tn.esprit.espritconnect2.Controller.emailBackOffice;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.BirthdayEmailSettingsRequestDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.BirthdayEmailSettingsResponseDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.SendTestBirthdayEmailRequestDTO;
import tn.esprit.espritconnect2.Service.emailBackOffice.communications.IBirthdayEmailSettingsService;

@RestController
@RequestMapping("/api/email-communications/automatic-emails/birthday")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Email Communications - Birthday", description = "Email automatique anniversaire (config + test)")
public class BirthdayEmailSettingsController {

    private final IBirthdayEmailSettingsService service;

    @GetMapping
    public ResponseEntity<BirthdayEmailSettingsResponseDTO> get() {
        return ResponseEntity.ok(service.get());
    }

    @PutMapping
    public ResponseEntity<BirthdayEmailSettingsResponseDTO> update(@Valid @RequestBody BirthdayEmailSettingsRequestDTO dto) {
        return ResponseEntity.ok(service.update(dto));
    }

    @PostMapping("/test-send")
    public ResponseEntity<Void> test(@Valid @RequestBody SendTestBirthdayEmailRequestDTO dto) {
        service.sendTest(dto);
        return ResponseEntity.ok().build();
    }
}
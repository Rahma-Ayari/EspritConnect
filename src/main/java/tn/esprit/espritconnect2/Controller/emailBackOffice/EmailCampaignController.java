package tn.esprit.espritconnect2.Controller.emailBackOffice;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.EmailCampaignRequestDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.EmailCampaignResponseDTO;
import tn.esprit.espritconnect2.Service.emailBackOffice.communications.IEmailCampaignService;

import java.util.List;

@RestController
@RequestMapping("/api/email-communications/message-users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Email Communications - Message users", description = "Campagnes email admin (bulk)")
public class EmailCampaignController {

    private final IEmailCampaignService service;

    @PostMapping
    public ResponseEntity<EmailCampaignResponseDTO> create(@Valid @RequestBody EmailCampaignRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailCampaignResponseDTO> update(@PathVariable Long id, @Valid @RequestBody EmailCampaignRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailCampaignResponseDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<EmailCampaignResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    /** Déclenche l'envoi réel (attention en démo : envoie vraiment des emails si SMTP OK) */
    @PostMapping("/{id}/send")
    public ResponseEntity<Void> send(@PathVariable Long id) {
        service.send(id);
        return ResponseEntity.ok().build();
    }
}
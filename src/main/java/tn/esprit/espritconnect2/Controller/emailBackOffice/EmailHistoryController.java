package tn.esprit.espritconnect2.Controller.emailBackOffice;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.EmailHistoryResponseDTO;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailHistoryType;
import tn.esprit.espritconnect2.Service.emailBackOffice.communications.IEmailHistoryService;

@RestController
@RequestMapping("/api/email-communications/history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Email Communications - History", description = "Historique des envois email")
public class EmailHistoryController {

    private final IEmailHistoryService service;

    @GetMapping
    public ResponseEntity<Page<EmailHistoryResponseDTO>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EmailHistoryType type
    ) {
        return ResponseEntity.ok(service.search(q, type, PageRequest.of(page, size)));
    }
}
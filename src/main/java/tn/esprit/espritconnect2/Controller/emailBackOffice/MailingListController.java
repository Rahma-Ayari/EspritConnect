package tn.esprit.espritconnect2.Controller.emailBackOffice;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.*;
import tn.esprit.espritconnect2.Service.emailBackOffice.communications.IMailingListService;

import java.util.List;

@RestController
@RequestMapping("/api/email-communications/mailing-lists")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Email Communications - Mailing lists", description = "Listes de diffusion + membres")
public class MailingListController {

    private final IMailingListService service;

    @PostMapping
    public ResponseEntity<MailingListResponseDTO> create(@Valid @RequestBody MailingListRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MailingListResponseDTO> update(@PathVariable Long id, @Valid @RequestBody MailingListRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MailingListResponseDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<MailingListResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<MailingListMemberResponseDTO> addMember(@PathVariable Long id, @Valid @RequestBody MailingListMemberRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addMember(id, dto));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<MailingListMemberResponseDTO>> members(@PathVariable Long id) {
        return ResponseEntity.ok(service.listMembers(id));
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
        service.removeMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
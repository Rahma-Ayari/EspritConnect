package tn.esprit.espritconnect2.Controller.backoffice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.Config.ApiOfficePaths;
import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Entitie.TicketStatus;
import tn.esprit.espritconnect2.Service.ISupportService;

import java.util.List;
import java.util.UUID;

/**
 * Backoffice — admins: ticket operations, FAQ management, seeding.
 */
@RestController
@RequestMapping(ApiOfficePaths.BACK_SUPPORT)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackSupportController {

    private final ISupportService supportService;

    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicketDTO>> getAllTickets() {
        return ResponseEntity.ok(supportService.getAllTicketsAdmin());
    }

    @GetMapping("/tickets/search")
    public ResponseEntity<List<SupportTicketDTO>> searchTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) tn.esprit.espritconnect2.Entitie.TicketPriority priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String searchQuery) {
        return ResponseEntity.ok(supportService.searchAndFilterTickets(status, priority, categoryId, searchQuery));
    }

    @PatchMapping("/tickets/{id}/assign/{adminId}")
    public ResponseEntity<SupportTicketDTO> assignTicket(@PathVariable Long id, @PathVariable UUID adminId) {
        return ResponseEntity.ok(supportService.assignTicket(id, adminId));
    }

    @PatchMapping("/tickets/{id}/status")
    public ResponseEntity<SupportTicketDTO> updateTicketStatus(@PathVariable Long id, @RequestParam TicketStatus status) {
        return ResponseEntity.ok(supportService.updateTicketStatus(id, status));
    }

    @PostMapping("/tickets/categories")
    public ResponseEntity<TicketCategoryDTO> createCategory(@RequestBody TicketCategoryDTO req) {
        return new ResponseEntity<>(supportService.createCategory(req), HttpStatus.CREATED);
    }

    @PutMapping("/tickets/categories/{id}")
    public ResponseEntity<TicketCategoryDTO> updateCategory(@PathVariable Long id, @RequestBody TicketCategoryDTO req) {
        return ResponseEntity.ok(supportService.updateCategory(id, req));
    }

    @DeleteMapping("/tickets/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        supportService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/faqs")
    public ResponseEntity<FAQDTO> createFAQ(@RequestBody FAQRequestDTO req) {
        return new ResponseEntity<>(supportService.createFAQ(req), HttpStatus.CREATED);
    }

    @PutMapping("/faqs/{id}")
    public ResponseEntity<FAQDTO> updateFAQ(@PathVariable Long id, @RequestBody FAQRequestDTO req) {
        return ResponseEntity.ok(supportService.updateFAQ(id, req));
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<Void> deleteFAQ(@PathVariable Long id) {
        supportService.deleteFAQ(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/faqs/{id}/important")
    public ResponseEntity<FAQDTO> markFAQImportant(@PathVariable Long id, @RequestParam boolean important) {
        return ResponseEntity.ok(supportService.markFAQImportant(id, important));
    }

    @PostMapping("/seed")
    public ResponseEntity<Void> seedData() {
        supportService.seedData();
        return ResponseEntity.ok().build();
    }
}

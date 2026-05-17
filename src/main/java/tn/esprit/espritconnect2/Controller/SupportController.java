package tn.esprit.espritconnect2.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Entitie.TicketStatus;
import tn.esprit.espritconnect2.Service.ISupportService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final ISupportService supportService;

    // --- Tickets ---

    @PostMapping("/tickets")
    public ResponseEntity<SupportTicketDTO> createTicket(@RequestBody SupportTicketRequestDTO req, @RequestParam UUID creatorId) {
        // In a real scenario with auth context, creatorId should come from JWT token
        return new ResponseEntity<>(supportService.createTicket(req, creatorId), HttpStatus.CREATED);
    }

    @GetMapping("/tickets/my")
    public ResponseEntity<List<SupportTicketDTO>> getMyTickets(@RequestParam UUID creatorId) {
        // In a real scenario with auth context, creatorId should come from JWT token
        return ResponseEntity.ok(supportService.getMyTickets(creatorId));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<SupportTicketDTO> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(supportService.getTicketById(id));
    }

    @GetMapping("/tickets/admin/all")
    public ResponseEntity<List<SupportTicketDTO>> getAllTicketsAdmin() {
        return ResponseEntity.ok(supportService.getAllTicketsAdmin());
    }

    @PatchMapping("/tickets/{id}/assign/{adminId}")
    public ResponseEntity<SupportTicketDTO> assignTicket(@PathVariable Long id, @PathVariable UUID adminId) {
        return ResponseEntity.ok(supportService.assignTicket(id, adminId));
    }

    @PatchMapping("/tickets/{id}/status")
    public ResponseEntity<SupportTicketDTO> updateTicketStatus(@PathVariable Long id, @RequestParam TicketStatus status) {
        return ResponseEntity.ok(supportService.updateTicketStatus(id, status));
    }

    // --- Messages ---

    @PostMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<TicketMessageDTO> addMessage(
            @PathVariable Long ticketId, 
            @RequestBody TicketMessageRequestDTO req, 
            @RequestParam UUID senderId) {
        // In a real scenario with auth context, senderId should come from JWT token
        return new ResponseEntity<>(supportService.addMessage(ticketId, req, senderId), HttpStatus.CREATED);
    }

    @GetMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<List<TicketMessageDTO>> getMessages(@PathVariable Long ticketId) {
        return ResponseEntity.ok(supportService.getMessages(ticketId));
    }

    // --- Categories ---

    @GetMapping("/tickets/categories")
    public ResponseEntity<List<TicketCategoryDTO>> getCategories() {
        return ResponseEntity.ok(supportService.getAllCategories());
    }

    @PostMapping("/tickets/admin/categories")
    public ResponseEntity<TicketCategoryDTO> createCategory(@RequestBody TicketCategoryDTO req) {
        return new ResponseEntity<>(supportService.createCategory(req), HttpStatus.CREATED);
    }

    @PutMapping("/tickets/admin/categories/{id}")
    public ResponseEntity<TicketCategoryDTO> updateCategory(@PathVariable Long id, @RequestBody TicketCategoryDTO req) {
        return ResponseEntity.ok(supportService.updateCategory(id, req));
    }

    @DeleteMapping("/tickets/admin/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        supportService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // --- FAQ ---

    @GetMapping("/faqs")
    public ResponseEntity<List<FAQDTO>> getAllFAQs() {
        return ResponseEntity.ok(supportService.getAllFAQs());
    }

    @GetMapping("/faqs/search")
    public ResponseEntity<List<FAQDTO>> searchFAQs(@RequestParam String query) {
        return ResponseEntity.ok(supportService.searchFAQs(query));
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

    @PostMapping("/seed")
    public ResponseEntity<Void> seedData() {
        supportService.seedData();
        return ResponseEntity.ok().build();
    }
}

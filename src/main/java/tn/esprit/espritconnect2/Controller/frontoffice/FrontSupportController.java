package tn.esprit.espritconnect2.Controller.frontoffice;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.Config.ApiOfficePaths;
import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Service.ISupportService;
import tn.esprit.espritconnect2.Service.TicketAttachmentStorage;
import tn.esprit.espritconnect2.security.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Frontoffice — students, alumni, companies: help center, tickets, chatbot.
 */
@RestController
@RequestMapping({ApiOfficePaths.FRONT_SUPPORT, "/api/support"})
@RequiredArgsConstructor
public class FrontSupportController {

    private final ISupportService supportService;
    private final TicketAttachmentStorage attachmentStorage;

    @PostMapping("/tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupportTicketDTO> createTicket(
            @RequestBody SupportTicketRequestDTO req,
            @RequestParam(required = false) UUID creatorId) {
        return new ResponseEntity<>(
                supportService.createTicket(req, SecurityUtils.getCurrentUserIdOr(creatorId)),
                HttpStatus.CREATED);
    }

    @GetMapping("/tickets/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SupportTicketDTO>> getMyTickets(@RequestParam(required = false) UUID creatorId) {
        return ResponseEntity.ok(supportService.getMyTickets(SecurityUtils.getCurrentUserIdOr(creatorId)));
    }

    @GetMapping("/tickets/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupportTicketDTO> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(supportService.getTicketById(id));
    }

    @PostMapping("/tickets/{id}/reopen")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupportTicketDTO> reopenTicket(
            @PathVariable Long id,
            @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(supportService.reopenTicket(id, SecurityUtils.getCurrentUserIdOr(userId)));
    }

    @GetMapping("/tickets/{id}/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportTicketHistory(@PathVariable Long id) {
        byte[] data = supportService.exportTicketHistory(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "ticket_history_" + id + ".txt");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/tickets/{ticketId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketMessageDTO> addMessage(
            @PathVariable Long ticketId,
            @RequestBody TicketMessageRequestDTO req,
            @RequestParam(required = false) UUID senderId) {
        return new ResponseEntity<>(
                supportService.addMessage(ticketId, req, SecurityUtils.getCurrentUserIdOr(senderId)),
                HttpStatus.CREATED);
    }

    @GetMapping("/tickets/{ticketId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketMessageDTO>> getMessages(@PathVariable Long ticketId) {
        return ResponseEntity.ok(supportService.getMessages(ticketId));
    }

    @GetMapping("/tickets/categories")
    public ResponseEntity<List<TicketCategoryDTO>> getCategories() {
        return ResponseEntity.ok(supportService.getAllCategories());
    }

    @GetMapping("/faqs")
    public ResponseEntity<List<FAQDTO>> getAllFAQs() {
        return ResponseEntity.ok(supportService.getAllFAQs());
    }

    @GetMapping("/faqs/search")
    public ResponseEntity<List<FAQDTO>> searchFAQs(@RequestParam String query) {
        return ResponseEntity.ok(supportService.searchFAQs(query));
    }

    @GetMapping("/faqs/popular")
    public ResponseEntity<List<FAQDTO>> getPopularFAQs() {
        return ResponseEntity.ok(supportService.getPopularFAQs());
    }

    @GetMapping("/faqs/important")
    public ResponseEntity<List<FAQDTO>> getImportantFAQs() {
        return ResponseEntity.ok(supportService.getImportantFAQs());
    }

    @GetMapping("/faqs/category/{categoryId}")
    public ResponseEntity<List<FAQDTO>> getFAQsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(supportService.getFAQsByCategory(categoryId));
    }

    @PostMapping("/faqs/{id}/view")
    public ResponseEntity<FAQDTO> incrementFAQView(@PathVariable Long id) {
        return ResponseEntity.ok(supportService.incrementFAQView(id));
    }

    @PostMapping("/faqs/{id}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FAQDTO> voteOnFAQ(@PathVariable Long id, @RequestParam boolean helpful) {
        return ResponseEntity.ok(supportService.voteOnFAQ(id, helpful));
    }

    @PostMapping("/faqs/community")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FAQDTO> submitCommunityFaq(
            @RequestBody FAQRequestDTO request,
            @RequestParam(required = false) UUID authorId) {
        return new ResponseEntity<>(
                supportService.submitCommunityFaq(request, SecurityUtils.getCurrentUserIdOr(authorId)),
                HttpStatus.CREATED);
    }

    @GetMapping("/faqs/{id}/comments")
    public ResponseEntity<List<FaqCommentDTO>> getFaqComments(@PathVariable Long id) {
        return ResponseEntity.ok(supportService.getFaqComments(id));
    }

    @PostMapping("/faqs/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FaqCommentDTO> addFaqComment(
            @PathVariable Long id,
            @RequestBody FaqCommentRequestDTO request,
            @RequestParam(required = false) UUID authorId) {
        return new ResponseEntity<>(
                supportService.addFaqComment(id, request, SecurityUtils.getCurrentUserIdOr(authorId)),
                HttpStatus.CREATED);
    }

    @PostMapping("/chatbot/ask")
    public ResponseEntity<Map<String, Object>> askChatbot(@RequestBody ChatbotRequestDTO request) {
        String message = request != null ? request.getMessage() : null;
        var history = request != null && request.getHistory() != null
                ? request.getHistory() : List.<ChatbotHistoryItemDTO>of();
        return ResponseEntity.ok(supportService.askChatbot(message, history));
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(attachmentStorage.store(file));
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request) {
        try {
            Resource resource = attachmentStorage.loadAsResource(filename, request);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(attachmentStorage.resolveMediaType(resource, request))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

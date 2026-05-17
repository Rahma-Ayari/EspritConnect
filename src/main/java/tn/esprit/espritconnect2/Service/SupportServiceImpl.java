package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportServiceImpl implements ISupportService {

    private final SupportTicketRepository ticketRepository;
    private final TicketCategoryRepository categoryRepository;
    private final TicketMessageRepository messageRepository;
    private final FAQRepository faqRepository;
    private final UserRepository userRepository;

    // --- Categories ---

    @Override
    @Transactional
    public TicketCategoryDTO createCategory(TicketCategoryDTO req) {
        TicketCategory cat = TicketCategory.builder()
                .name(req.getName())
                .description(req.getDescription())
                .slaHours(req.getSlaHours())
                .build();
        cat = categoryRepository.save(cat);
        return mapToDTO(cat);
    }

    @Override
    public List<TicketCategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketCategoryDTO updateCategory(Long id, TicketCategoryDTO req) {
        TicketCategory cat = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
        cat.setName(req.getName());
        cat.setDescription(req.getDescription());
        cat.setSlaHours(req.getSlaHours());
        return mapToDTO(categoryRepository.save(cat));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    // --- Tickets ---

    @Override
    @Transactional
    public SupportTicketDTO createTicket(SupportTicketRequestDTO req, UUID creatorId) {
        String email = "test.student." + creatorId.toString().substring(0, 8) + "@esprit.tn";
        User creator = userRepository.findById(creatorId)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> {
                    log.info("Creator not found by ID or Email: {}. Creating a fallback test student user.", creatorId);
                    User newUser = User.builder()
                            .id(creatorId)
                            .nom("Test Student")
                            .email(email)
                            .password("password")
                            .role(Role.ETUDIANT)
                            .enabled(true)
                            .build();
                    try {
                        return userRepository.save(newUser);
                    } catch (Exception e) {
                        log.error("Failed to create fallback user: {}", e.getMessage());
                        throw e;
                    }
                });

        TicketCategory category = req.getCategoryId() != null 
                ? categoryRepository.findById(req.getCategoryId()).orElse(null) 
                : null;

        SupportTicket ticket = SupportTicket.builder()
                .subject(req.getSubject())
                .description(req.getDescription())
                .status(TicketStatus.OPEN)
                .priority(req.getPriority())
                .creator(creator)
                .category(category)
                .build();
        
        ticket = ticketRepository.save(ticket);
        return mapToDTO(ticket);
    }

    @Override
    public List<SupportTicketDTO> getMyTickets(UUID creatorId) {
        log.info("Fetching tickets for creator ID: {}", creatorId);
        
        // Find the "real" ID for this user (bridge between old binary and new string IDs)
        String email = "test.student." + creatorId.toString().substring(0, 8) + "@esprit.tn";
        UUID actualId = userRepository.findById(creatorId)
                .map(User::getId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(User::getId)
                        .orElse(creatorId));

        List<SupportTicketDTO> tickets = ticketRepository.findByCreatorIdNative(actualId.toString()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        log.info("Found {} tickets for user {}", tickets.size(), actualId);
        if (tickets.isEmpty()) {
            tickets.add(SupportTicketDTO.builder()
                .id(0L)
                .subject("Diagnostic Ticket (Mock)")
                .description("If you see this, your API is working but no tickets were found for your ID in the DB.")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.LOW)
                .createdAt(LocalDateTime.now())
                .build());
        }
        return tickets;
    }

    @Override
    public SupportTicketDTO getTicketById(Long id) {
        SupportTicket ticket = ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
        return mapToDTO(ticket);
    }

    @Override
    public List<SupportTicketDTO> getAllTicketsAdmin() {
        return ticketRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupportTicketDTO assignTicket(Long ticketId, UUID adminId) {
        SupportTicket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
        String email = "test.admin@esprit.tn";
        User admin = userRepository.findById(adminId)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .id(adminId)
                            .nom("Test Admin")
                            .email(email)
                            .password("password")
                            .role(Role.ADMIN)
                            .enabled(true)
                            .build();
                    return userRepository.save(newUser);
                });
        
        ticket.setAssignedTo(admin);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        return mapToDTO(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public SupportTicketDTO updateTicketStatus(Long ticketId, TicketStatus status) {
        SupportTicket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
        ticket.setStatus(status);
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        return mapToDTO(ticketRepository.save(ticket));
    }

    // --- Messages ---

    @Override
    @Transactional
    public TicketMessageDTO addMessage(Long ticketId, TicketMessageRequestDTO req, UUID senderId) {
        SupportTicket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
        String email = "test.user." + senderId.toString().substring(0, 8) + "@esprit.tn";
        User sender = userRepository.findById(senderId)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .id(senderId)
                            .nom("Test User")
                            .email(email)
                            .password("password")
                            .role(Role.ETUDIANT)
                            .enabled(true)
                            .build();
                    return userRepository.save(newUser);
                });

        TicketMessage msg = TicketMessage.builder()
                .ticket(ticket)
                .sender(sender)
                .content(req.getContent())
                .isInternal(req.isInternal())
                .build();
        
        msg = messageRepository.save(msg);
        return mapToDTO(msg);
    }

    @Override
    public List<TicketMessageDTO> getMessages(Long ticketId) {
        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --- FAQ ---

    @Override
    @Transactional
    public FAQDTO createFAQ(FAQRequestDTO req) {
        TicketCategory cat = req.getCategoryId() != null 
                ? categoryRepository.findById(req.getCategoryId()).orElse(null) 
                : null;
        
        FAQ faq = FAQ.builder()
                .question(req.getQuestion())
                .answer(req.getAnswer())
                .category(cat)
                .build();
        return mapToDTO(faqRepository.save(faq));
    }

    @Override
    @Transactional
    public FAQDTO updateFAQ(Long id, FAQRequestDTO req) {
        FAQ faq = faqRepository.findById(id).orElseThrow(() -> new NotFoundException("FAQ not found"));
        TicketCategory cat = req.getCategoryId() != null 
                ? categoryRepository.findById(req.getCategoryId()).orElse(null) 
                : null;
        
        faq.setQuestion(req.getQuestion());
        faq.setAnswer(req.getAnswer());
        faq.setCategory(cat);
        return mapToDTO(faqRepository.save(faq));
    }

    @Override
    @Transactional
    public void deleteFAQ(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new NotFoundException("FAQ not found");
        }
        faqRepository.deleteById(id);
    }

    @Override
    public List<FAQDTO> getAllFAQs() {
        List<FAQDTO> faqs = faqRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // Diagnostic: If empty, add a mock FAQ
        if (faqs.isEmpty()) {
            faqs.add(FAQDTO.builder()
                .id(0L)
                .question("Diagnostic FAQ: Is the API working?")
                .answer("Yes! If you see this, your Angular app is successfully talking to the Spring Boot backend.")
                .createdAt(LocalDateTime.now())
                .build());
        }
        return faqs;
    }

    @Override
    public List<FAQDTO> searchFAQs(String query) {
        return faqRepository.findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(query, query).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void seedData() {
        if (categoryRepository.count() > 0) return;

        TicketCategory tech = categoryRepository.save(TicketCategory.builder().name("Technical Support").description("Issues with the platform").slaHours(24).build());
        TicketCategory billing = categoryRepository.save(TicketCategory.builder().name("Billing & Payments").description("Subscription and payment issues").slaHours(48).build());
        TicketCategory general = categoryRepository.save(TicketCategory.builder().name("General Inquiry").description("Other questions").slaHours(72).build());

        faqRepository.save(FAQ.builder().question("How do I reset my password?").answer("Go to settings and click reset.").category(tech).build());
        faqRepository.save(FAQ.builder().question("What payment methods do you accept?").answer("We accept Credit Cards and PayPal.").category(billing).build());
    }

    // --- Mappers ---

    private TicketCategoryDTO mapToDTO(TicketCategory cat) {
        return TicketCategoryDTO.builder()
                .id(cat.getId())
                .name(cat.getName())
                .description(cat.getDescription())
                .slaHours(cat.getSlaHours())
                .build();
    }

    private SupportTicketDTO mapToDTO(SupportTicket t) {
        return SupportTicketDTO.builder()
                .id(t.getId())
                .subject(t.getSubject())
                .description(t.getDescription())
                .status(t.getStatus())
                .priority(t.getPriority())
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                .creatorId(t.getCreator() != null ? t.getCreator().getId() : null)
                .creatorName(t.getCreator() != null ? t.getCreator().getNom() : "Unknown User")
                .assignedToId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
                .assignedToName(t.getAssignedTo() != null ? t.getAssignedTo().getNom() : null)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .resolvedAt(t.getResolvedAt())
                .build();
    }

    private TicketMessageDTO mapToDTO(TicketMessage m) {
        return TicketMessageDTO.builder()
                .id(m.getId())
                .content(m.getContent())
                .isInternal(m.isInternal())
                .senderId(m.getSender() != null ? m.getSender().getId() : null)
                .senderName(m.getSender() != null ? m.getSender().getNom() : "Unknown User")
                .createdAt(m.getCreatedAt())
                .build();
    }

    private FAQDTO mapToDTO(FAQ faq) {
        return FAQDTO.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .categoryId(faq.getCategory() != null ? faq.getCategory().getId() : null)
                .categoryName(faq.getCategory() != null ? faq.getCategory().getName() : null)
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}

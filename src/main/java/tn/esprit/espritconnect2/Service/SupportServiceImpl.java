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
    private final FaqCommentRepository faqCommentRepository;
    private final UserRepository userRepository;
    private final TicketHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;
    private final jakarta.persistence.EntityManager entityManager;
    private final FaqRetrievalService faqRetrievalService;
    private final ChatbotAiService chatbotAiService;
    private final tn.esprit.espritconnect2.Config.ChatbotProperties chatbotProperties;

    private TicketPriority detectSmartPriority(String title, String description, TicketPriority requested) {
        if (title == null) title = "";
        if (description == null) description = "";
        String text = (title + " " + description).toLowerCase();
        if (text.contains("urgent") || text.contains("hacked") || text.contains("cannot login") || text.contains("security")) {
            return TicketPriority.HIGH;
        }
        return requested != null ? requested : TicketPriority.LOW;
    }

    private void logHistory(SupportTicket ticket, String eventType, String description, String performedBy) {
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .eventType(eventType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .performedBy(performedBy)
                .build();
        historyRepository.save(history);
    }

    private void sendNotification(String type, String content, String recipientEmail) {
        try {
            Notification notification = new Notification();
            notification.setType(type);
            notification.setContenu(content);
            notification.setDestinataire(recipientEmail);
            notification.setDateEnvoi(new java.util.Date());
            notification.setLue(false);
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to send ticket notification: {}", e.getMessage());
        }
    }

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
                .title(req.getTitle())
                .description(req.getDescription())
                .status(TicketStatus.OPEN)
                .priority(detectSmartPriority(req.getTitle(), req.getDescription(), req.getPriority()))
                .attachmentUrl(req.getAttachmentUrl())
                .tags(req.getTags())
                .creator(creator)
                .category(category)
                .build();
        
        ticket = ticketRepository.save(ticket);
        logHistory(ticket, "CREATED", "Ticket created by " + creator.getNom(), creator.getNom());
        sendNotification("TICKET_CREATED", "Ticket #" + ticket.getId() + " has been successfully created: " + ticket.getTitle(), creator.getEmail());
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

        List<SupportTicketDTO> tickets = ticketRepository.findByCreatorId(actualId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        log.info("Found {} tickets for user {}", tickets.size(), actualId);
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
        boolean statusChanged = false;
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            statusChanged = true;
        }
        ticket = ticketRepository.save(ticket);
        
        logHistory(ticket, "ASSIGNED", "Ticket assigned to support agent " + admin.getNom(), admin.getNom());
        if (statusChanged) {
            logHistory(ticket, "STATUS_CHANGED", "Status changed to IN_PROGRESS", admin.getNom());
        }
        
        if (ticket.getCreator() != null) {
            sendNotification("TICKET_ASSIGNED", "Your ticket #" + ticket.getId() + " has been assigned to support agent " + admin.getNom(), ticket.getCreator().getEmail());
        }
        
        return mapToDTO(ticket);
    }

    @Override
    @Transactional
    public SupportTicketDTO updateTicketStatus(Long ticketId, TicketStatus status) {
        SupportTicket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(status);
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        ticket = ticketRepository.save(ticket);
        
        logHistory(ticket, "STATUS_CHANGED", "Status changed from " + oldStatus + " to " + status, "System");
        if (status == TicketStatus.RESOLVED) {
            logHistory(ticket, "RESOLVED", "Ticket marked as resolved", "System");
        }
        
        if (ticket.getCreator() != null) {
            sendNotification("TICKET_STATUS", "Your ticket #" + ticket.getId() + " status updated to " + status, ticket.getCreator().getEmail());
        }
        
        return mapToDTO(ticket);
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
                .attachmentUrl(req.getAttachmentUrl())
                .build();
        
        msg = messageRepository.save(msg);
        
        boolean isAdmin = sender.getRole() == Role.ADMIN;
        String eventType = isAdmin ? "ADMIN_REPLY" : "USER_REPLY";
        String desc = isAdmin ? "Admin " + sender.getNom() + " replied to the ticket" : "User " + sender.getNom() + " sent a message";
        logHistory(ticket, eventType, desc, sender.getNom());
        
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        
        if (isAdmin) {
            if (ticket.getCreator() != null) {
                sendNotification("TICKET_REPLY", "Support Agent " + sender.getNom() + " replied to your ticket #" + ticket.getId(), ticket.getCreator().getEmail());
            }
        } else {
            if (ticket.getAssignedTo() != null) {
                sendNotification("TICKET_REPLY", "User " + sender.getNom() + " sent a message on ticket #" + ticket.getId(), ticket.getAssignedTo().getEmail());
            }
        }
        
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
                .isImportant(req.isImportant())
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
        faq.setImportant(req.isImportant());
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

        return faqs;
    }

    @Override
    public List<FAQDTO> searchFAQs(String query) {
        return faqRepository.findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(query, query).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FAQDTO> getFAQsByCategory(Long categoryId) {
        return faqRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FAQDTO> getPopularFAQs() {
        return faqRepository.findTop5ByOrderByViewCountDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FAQDTO> getImportantFAQs() {
        return faqRepository.findByIsImportantTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FAQDTO incrementFAQView(Long id) {
        faqRepository.incrementViewCount(id);
        return mapToDTO(faqRepository.findById(id).orElseThrow(() -> new NotFoundException("FAQ not found")));
    }

    @Override
    @Transactional
    public FAQDTO voteOnFAQ(Long id, boolean helpful) {
        if (helpful) {
            faqRepository.incrementHelpfulCount(id);
        } else {
            faqRepository.incrementNotHelpfulCount(id);
        }
        return mapToDTO(faqRepository.findById(id).orElseThrow(() -> new NotFoundException("FAQ not found")));
    }

    @Override
    @Transactional
    public FAQDTO markFAQImportant(Long id, boolean important) {
        FAQ faq = faqRepository.findById(id).orElseThrow(() -> new NotFoundException("FAQ not found"));
        faq.setImportant(important);
        return mapToDTO(faqRepository.save(faq));
    }

    @Override
    @Transactional
    public FAQDTO submitCommunityFaq(FAQRequestDTO req, UUID authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        TicketCategory cat = req.getCategoryId() != null
                ? categoryRepository.findById(req.getCategoryId()).orElse(null)
                : null;

        FAQ faq = FAQ.builder()
                .question(req.getQuestion())
                .answer(req.getAnswer())
                .category(cat)
                .author(author)
                .isImportant(false)
                .build();
        return mapToDTO(faqRepository.save(faq));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqCommentDTO> getFaqComments(Long faqId) {
        if (!faqRepository.existsById(faqId)) {
            throw new NotFoundException("FAQ not found");
        }
        return faqCommentRepository.findByFaqIdOrderByCreatedAtAsc(faqId).stream()
                .map(this::mapCommentToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FaqCommentDTO addFaqComment(Long faqId, FaqCommentRequestDTO req, UUID authorId) {
        FAQ faq = faqRepository.findById(faqId).orElseThrow(() -> new NotFoundException("FAQ not found"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new tn.esprit.espritconnect2.Exception.BusinessRuleException("Comment cannot be empty.");
        }

        FaqComment comment = FaqComment.builder()
                .faq(faq)
                .author(author)
                .content(req.getContent().trim())
                .build();
        return mapCommentToDTO(faqCommentRepository.save(comment));
    }

    @Override
    @Transactional
    public void seedData() {
        if (categoryRepository.count() > 0) {
            if (faqRepository.count() >= 5) {
                return;
            }
            seedDefaultFaqs(categoryRepository.findAll().stream()
                    .filter(c -> "Authentication Problems".equals(c.getName())).findFirst().orElse(null),
                    categoryRepository.findAll().stream()
                            .filter(c -> "Account Issues".equals(c.getName())).findFirst().orElse(null),
                    categoryRepository.findAll().stream()
                            .filter(c -> "Job Application Problems".equals(c.getName())).findFirst().orElse(null),
                    categoryRepository.findAll().stream()
                            .filter(c -> "Event Problems".equals(c.getName())).findFirst().orElse(null),
                    categoryRepository.findAll().stream()
                            .filter(c -> "Technical Bugs".equals(c.getName())).findFirst().orElse(null),
                    categoryRepository.findAll().stream()
                            .filter(c -> "Content Report".equals(c.getName())).findFirst().orElse(null));
            return;
        }

        TicketCategory acc = categoryRepository.save(TicketCategory.builder().name("Account Issues").description("Problems related to accounts").slaHours(24).build());
        TicketCategory auth = categoryRepository.save(TicketCategory.builder().name("Authentication Problems").description("Login, signup, or password reset problems").slaHours(24).build());
        TicketCategory evt = categoryRepository.save(TicketCategory.builder().name("Event Problems").description("Problems with events and workshops").slaHours(48).build());
        TicketCategory job = categoryRepository.save(TicketCategory.builder().name("Job Application Problems").description("Problems with job offers or applications").slaHours(48).build());
        TicketCategory forum = categoryRepository.save(TicketCategory.builder().name("Forum Issues").description("Problems posting or reading on the forum").slaHours(48).build());
        TicketCategory bug = categoryRepository.save(TicketCategory.builder().name("Technical Bugs").description("Bugs or glitches on the platform").slaHours(24).build());
        TicketCategory report = categoryRepository.save(TicketCategory.builder().name("Content Report").description("Reporting inappropriate or incorrect content").slaHours(72).build());
        TicketCategory suggestion = categoryRepository.save(TicketCategory.builder().name("Suggestion/Improvement").description("Feedback and suggestions for new features").slaHours(72).build());
        TicketCategory security = categoryRepository.save(TicketCategory.builder().name("Security Issue").description("Security bugs or vulnerability reports").slaHours(12).build());

        seedDefaultFaqs(auth, acc, job, evt, bug, report);
    }

    private void seedDefaultFaqs(TicketCategory auth, TicketCategory acc, TicketCategory job,
                                 TicketCategory evt, TicketCategory bug, TicketCategory report) {
        if (auth == null && acc == null) {
            return;
        }
        saveFaqIfMissing("How do I reset my password?",
                "Go to Login, click 'Forgot password', enter your email, and follow the link sent to your inbox. Links expire after 24 hours.",
                auth, true);
        saveFaqIfMissing("How do I create an account on ESPRIT Connect?",
                "Click Register on the login page, choose your role (Student, Alumni, or Company), fill in the required fields, and verify your email.",
                acc, false);
        saveFaqIfMissing("What should I do if a job application fails?",
                "Ensure your CV is PDF or DOCX under 5 MB, your profile is complete, and the offer is still open. If the error persists, submit a support ticket with a screenshot.",
                job, true);
        saveFaqIfMissing("How do I register for an event or workshop?",
                "Open Events from the menu, select the event, and click Register. You will receive a confirmation notification.",
                evt, false);
        saveFaqIfMissing("How do I submit a support ticket?",
                "Go to Support > Submit Ticket, choose a category, describe your issue clearly, and attach screenshots if helpful. You can track status from My Tickets.",
                bug, true);
        saveFaqIfMissing("How do I update my profile or CV?",
                "Open your Profile page, edit bio, skills, and links, then upload a new CV from the Documents section. Save changes before leaving the page.",
                acc, false);
        saveFaqIfMissing("I found inappropriate content on the forum. What should I do?",
                "Use Report on the post or submit a Content Report ticket with the post link and a short description. Moderators will review it.",
                report, false);
    }

    private void saveFaqIfMissing(String question, String answer, TicketCategory category, boolean important) {
        if (category == null || faqRepository.findAll().stream()
                .anyMatch(f -> question.equalsIgnoreCase(f.getQuestion()))) {
            return;
        }
        faqRepository.save(FAQ.builder()
                .question(question)
                .answer(answer)
                .category(category)
                .isImportant(important)
                .build());
    }

    @Override
    @Transactional
    public SupportTicketDTO reopenTicket(Long ticketId, UUID userId) {
        SupportTicket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
        User user = userRepository.findById(userId).orElse(null);
        String name = user != null ? user.getNom() : "User";
        
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setResolvedAt(null);
        ticket = ticketRepository.save(ticket);
        
        logHistory(ticket, "REOPENED", "Ticket reopened by " + name, name);
        
        if (ticket.getAssignedTo() != null) {
            sendNotification("TICKET_REOPENED", "Ticket #" + ticket.getId() + " has been reopened by user " + name, ticket.getAssignedTo().getEmail());
        }
        
        return mapToDTO(ticket);
    }

    @Override
    public byte[] exportTicketHistory(Long ticketId) {
        SupportTicket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
        SupportTicketDTO dto = mapToDTO(ticket);
        List<TicketMessage> messages = messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        List<TicketHistory> timeline = historyRepository.findByTicketIdOrderByTimestampAsc(ticketId);
        
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("         SUPPORT TICKET HISTORY REPORT            \n");
        sb.append("==================================================\n\n");
        
        sb.append("TICKET DETAILS:\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Ticket ID:       ").append(ticket.getId()).append("\n");
        sb.append("Title:           ").append(ticket.getTitle()).append("\n");
        sb.append("Status:          ").append(ticket.getStatus()).append("\n");
        sb.append("Priority:        ").append(ticket.getPriority()).append("\n");
        sb.append("Category:        ").append(ticket.getCategory() != null ? ticket.getCategory().getName() : "None").append("\n");
        sb.append("Creator:         ").append(ticket.getCreator() != null ? ticket.getCreator().getNom() : "Unknown").append(" (").append(ticket.getCreator() != null ? ticket.getCreator().getEmail() : "").append(")\n");
        sb.append("Assigned To:     ").append(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getNom() : "Unassigned").append("\n");
        sb.append("Created At:      ").append(ticket.getCreatedAt()).append("\n");
        sb.append("Updated At:      ").append(ticket.getUpdatedAt()).append("\n");
        sb.append("Resolved At:     ").append(ticket.getResolvedAt() != null ? ticket.getResolvedAt() : "N/A").append("\n");
        sb.append("SLA Status:      ").append(dto.getSlaMessage()).append("\n\n");
        
        sb.append("DESCRIPTION:\n");
        sb.append("--------------------------------------------------\n");
        sb.append(ticket.getDescription()).append("\n\n");
        
        sb.append("TIMELINE EVENTS:\n");
        sb.append("--------------------------------------------------\n");
        if (timeline.isEmpty()) {
            sb.append("No timeline events logged.\n");
        } else {
            for (TicketHistory event : timeline) {
                sb.append("[").append(event.getTimestamp()).append("] ")
                  .append(event.getEventType()).append(": ")
                  .append(event.getDescription())
                  .append(" (by ").append(event.getPerformedBy()).append(")\n");
            }
        }
        sb.append("\n");
        
        sb.append("CONVERSATION THREAD:\n");
        sb.append("--------------------------------------------------\n");
        if (messages.isEmpty()) {
            sb.append("No messages in this ticket.\n");
        } else {
            for (TicketMessage msg : messages) {
                String senderRole = msg.getSender() != null ? msg.getSender().getRole().toString() : "UNKNOWN";
                String senderName = msg.getSender() != null ? msg.getSender().getNom() : "Unknown";
                sb.append("[").append(msg.getCreatedAt()).append("] ")
                  .append(senderName).append(" (").append(senderRole).append("):\n")
                  .append(msg.getContent()).append("\n");
                if (msg.getAttachmentUrl() != null) {
                    sb.append("Attachment: ").append(msg.getAttachmentUrl()).append("\n");
                }
                sb.append("--------------------------------------------------\n");
            }
        }
        
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public List<SupportTicketDTO> searchAndFilterTickets(TicketStatus status, TicketPriority priority, Long categoryId, String searchQuery) {
        StringBuilder jpql = new StringBuilder("SELECT t FROM SupportTicket t WHERE 1=1");
        
        if (status != null) {
            jpql.append(" AND t.status = :status");
        }
        if (priority != null) {
            jpql.append(" AND t.priority = :priority");
        }
        if (categoryId != null) {
            jpql.append(" AND t.category.id = :categoryId");
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            jpql.append(" AND (LOWER(t.title) LIKE :query OR LOWER(t.description) LIKE :query OR LOWER(t.creator.nom) LIKE :query)");
        }
        
        jpql.append(" ORDER BY t.createdAt DESC");
        
        jakarta.persistence.TypedQuery<SupportTicket> query = entityManager.createQuery(jpql.toString(), SupportTicket.class);
        
        if (status != null) {
            query.setParameter("status", status);
        }
        if (priority != null) {
            query.setParameter("priority", priority);
        }
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            query.setParameter("query", "%" + searchQuery.toLowerCase().trim() + "%");
        }
        
        return query.getResultList().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
        String slaMessage = "";
        try {
            List<TicketMessage> ticketMsgs = messageRepository.findByTicketIdOrderByCreatedAtAsc(t.getId());
            LocalDateTime firstAdminReplyTime = null;
            for (TicketMessage msg : ticketMsgs) {
                if (msg.getSender() != null && msg.getSender().getRole() == Role.ADMIN) {
                    firstAdminReplyTime = msg.getCreatedAt();
                    break;
                }
            }
            
            LocalDateTime baseTime = t.getCreatedAt() != null ? t.getCreatedAt() : LocalDateTime.now();
            if (firstAdminReplyTime != null) {
                java.time.Duration duration = java.time.Duration.between(baseTime, firstAdminReplyTime);
                long hours = duration.toHours();
                if (hours >= 24) {
                    long days = duration.toDays();
                    slaMessage = "Answered in " + days + " day" + (days > 1 ? "s" : "");
                } else if (hours > 0) {
                    slaMessage = "Answered in " + hours + " hour" + (hours > 1 ? "s" : "");
                } else {
                    long minutes = duration.toMinutes();
                    slaMessage = "Answered in " + minutes + " minute" + (minutes > 1 ? "s" : "");
                }
            } else {
                java.time.Duration duration = java.time.Duration.between(baseTime, LocalDateTime.now());
                long days = duration.toDays();
                if (days >= 1) {
                    slaMessage = "Pending for " + days + " day" + (days > 1 ? "s" : "");
                } else {
                    long hours = duration.toHours();
                    if (hours >= 1) {
                        slaMessage = "Pending for " + hours + " hour" + (hours > 1 ? "s" : "");
                    } else {
                        long minutes = duration.toMinutes();
                        slaMessage = "Pending for " + minutes + " minute" + (minutes > 1 ? "s" : "");
                    }
                }
            }
        } catch (Exception ex) {
            slaMessage = "Pending";
        }

        List<TicketHistoryDTO> timelineDTOs = List.of();
        try {
            timelineDTOs = historyRepository.findByTicketIdOrderByTimestampAsc(t.getId()).stream()
                    .map(h -> TicketHistoryDTO.builder()
                            .id(h.getId())
                            .eventType(h.getEventType())
                            .description(h.getDescription())
                            .timestamp(h.getTimestamp())
                            .performedBy(h.getPerformedBy())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            // Fallback
        }

        return SupportTicketDTO.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .priority(t.getPriority())
                .attachmentUrl(t.getAttachmentUrl())
                .tags(t.getTags())
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                .creatorId(t.getCreator() != null ? t.getCreator().getId() : null)
                .creatorName(t.getCreator() != null ? t.getCreator().getNom() : "Unknown User")
                .assignedToId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
                .assignedToName(t.getAssignedTo() != null ? t.getAssignedTo().getNom() : null)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .resolvedAt(t.getResolvedAt())
                .slaMessage(slaMessage)
                .timeline(timelineDTOs)
                .build();
    }

    private TicketMessageDTO mapToDTO(TicketMessage m) {
        return TicketMessageDTO.builder()
                .id(m.getId())
                .content(m.getContent())
                .isInternal(m.isInternal())
                .senderId(m.getSender() != null ? m.getSender().getId() : null)
                .senderName(m.getSender() != null ? m.getSender().getNom() : "Unknown User")
                .attachmentUrl(m.getAttachmentUrl())
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
                .authorName(faq.getAuthor() != null ? faq.getAuthor().getNom() : null)
                .isImportant(faq.isImportant())
                .viewCount(faq.getViewCount())
                .helpfulCount(faq.getHelpfulCount())
                .notHelpfulCount(faq.getNotHelpfulCount())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }

    private FaqCommentDTO mapCommentToDTO(FaqComment comment) {
        return FaqCommentDTO.builder()
                .id(comment.getId())
                .faqId(comment.getFaq().getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getNom())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Override
    public java.util.Map<String, Object> askChatbot(String message, List<ChatbotHistoryItemDTO> history) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        List<ChatbotHistoryItemDTO> safeHistory = history != null ? history : List.of();

        if (message == null || message.trim().isEmpty()) {
            response.put("response", "Hi! I'm your ESPRIT Connect assistant — ask me about careers, the platform, "
                    + "events, mentoring, study tips, or anything else. How can I help?");
            response.put("suggestedFaqs", List.of());
            response.put("ticketSuggest", false);
            response.put("aiPowered", false);
            return response;
        }

        try {
            List<FAQ> matchedFaqs = faqRetrievalService.findRelevantFaqs(message, chatbotProperties.getMaxFaqsContext());
            String knowledgeContext = faqRetrievalService.buildKnowledgeContext(matchedFaqs);

            String aiReply = chatbotAiService.generateReply(message.trim(), knowledgeContext, safeHistory);
            boolean aiPowered = aiReply != null && !aiReply.isBlank();

            if (!aiPowered && chatbotAiService.isConfigured()) {
                log.warn("Chatbot: AI configured but no reply for message='{}' — using fallback", message.trim());
            } else if (!chatbotAiService.isConfigured()) {
                log.warn("Chatbot: no API key configured — using FAQ/static fallback. Set GROQ_API_KEY or application-local.properties");
            }

            String finalReply = aiPowered
                    ? aiReply
                    : chatbotAiService.buildFallbackReply(message.trim(), matchedFaqs);

            boolean ticketSuggest = chatbotAiService.shouldSuggestTicket(message.trim(), finalReply);

            response.put("response", finalReply);
            response.put("suggestedFaqs", matchedFaqs.stream().map(this::mapToDTO).collect(Collectors.toList()));
            response.put("ticketSuggest", ticketSuggest);
            response.put("aiPowered", aiPowered);
        } catch (Exception e) {
            response.put("response", chatbotAiService.buildFallbackReply(message.trim(), List.of()));
            response.put("suggestedFaqs", List.of());
            response.put("ticketSuggest", false);
            response.put("aiPowered", false);
        }
        return response;
    }
}

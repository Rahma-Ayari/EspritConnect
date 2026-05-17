package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Entitie.TicketStatus;

import java.util.List;
import java.util.UUID;

public interface ISupportService {
    
    // Categories
    TicketCategoryDTO createCategory(TicketCategoryDTO req);
    TicketCategoryDTO updateCategory(Long id, TicketCategoryDTO req);
    void deleteCategory(Long id);
    List<TicketCategoryDTO> getAllCategories();
    
    // Tickets
    SupportTicketDTO createTicket(SupportTicketRequestDTO req, UUID creatorId);
    List<SupportTicketDTO> getMyTickets(UUID creatorId);
    SupportTicketDTO getTicketById(Long id);
    List<SupportTicketDTO> getAllTicketsAdmin();
    SupportTicketDTO assignTicket(Long ticketId, UUID adminId);
    SupportTicketDTO updateTicketStatus(Long ticketId, TicketStatus status);
    
    // Messages
    TicketMessageDTO addMessage(Long ticketId, TicketMessageRequestDTO req, UUID senderId);
    List<TicketMessageDTO> getMessages(Long ticketId);
    
    // FAQ
    FAQDTO createFAQ(FAQRequestDTO req);
    FAQDTO updateFAQ(Long id, FAQRequestDTO req);
    void deleteFAQ(Long id);
    List<FAQDTO> getAllFAQs();
    List<FAQDTO> searchFAQs(String query);
    void seedData();
}

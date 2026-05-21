package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.EmailHistoryResponseDTO;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailHistoryType;

public interface IEmailHistoryService {
    Page<EmailHistoryResponseDTO> search(String q, EmailHistoryType type, Pageable pageable);
}
package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.EmailCampaignRequestDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.EmailCampaignResponseDTO;

import java.util.List;

public interface IEmailCampaignService {
    EmailCampaignResponseDTO create(EmailCampaignRequestDTO dto);
    EmailCampaignResponseDTO update(Long id, EmailCampaignRequestDTO dto);
    EmailCampaignResponseDTO get(Long id);
    List<EmailCampaignResponseDTO> list();
    void send(Long id);
    void sendNow(EmailCampaignRequestDTO dto);
}
package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.BirthdayEmailSettingsRequestDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.BirthdayEmailSettingsResponseDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.SendTestBirthdayEmailRequestDTO;

public interface IBirthdayEmailSettingsService {
    BirthdayEmailSettingsResponseDTO get();
    BirthdayEmailSettingsResponseDTO update(BirthdayEmailSettingsRequestDTO dto);
    void sendTest(SendTestBirthdayEmailRequestDTO dto);
}
package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.GeneralSettingsDTO;
import tn.esprit.espritconnect2.DTO.RegionalSettingsDTO;
import tn.esprit.espritconnect2.DTO.RegistrationSettingsDTO;

public interface IPlatformSettingsService {
    GeneralSettingsDTO getGeneralSettings();
    GeneralSettingsDTO updateGeneralSettings(GeneralSettingsDTO dto);
    RegionalSettingsDTO getRegionalSettings();
    RegionalSettingsDTO updateRegionalSettings(RegionalSettingsDTO dto);
    RegistrationSettingsDTO getRegistrationSettings();
    RegistrationSettingsDTO updateRegistrationSettings(RegistrationSettingsDTO dto);
    void seedDefaultsIfMissing();
}

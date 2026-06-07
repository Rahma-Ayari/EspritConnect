package tn.esprit.espritconnect2.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.Service.GeneralSettingsService;
import tn.esprit.espritconnect2.Service.RegistrationSettingsService;
import tn.esprit.espritconnect2.Service.RegionalSettingsService;
import tn.esprit.espritconnect2.Service.ISupportService;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final ISupportService supportService;
    private final GeneralSettingsService generalSettingsService;
    private final RegionalSettingsService regionalSettingsService;
    private final RegistrationSettingsService registrationSettingsService;

    @Override
    public void run(String... args) throws Exception {
        generalSettingsService.getSettings();
        regionalSettingsService.getSettings();
        registrationSettingsService.getSettings();
        supportService.seedData();
    }
}

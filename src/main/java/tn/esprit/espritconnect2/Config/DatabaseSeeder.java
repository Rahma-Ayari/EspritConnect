package tn.esprit.espritconnect2.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.Service.ISupportService;
import tn.esprit.espritconnect2.Service.IPlatformSettingsService;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final ISupportService supportService;
    private final IPlatformSettingsService platformSettingsService;

    @Override
    public void run(String... args) throws Exception {
        platformSettingsService.seedDefaultsIfMissing();
        supportService.seedData();
    }
}

package tn.esprit.espritconnect2.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.Service.StudentAccountResolver;

/**
 * Ensures every ETUDIANT user account has a matching etudiant row (required for jobs & AI features).
 */
@Component
@Order(101)
@RequiredArgsConstructor
public class StudentProfileSyncRunner implements ApplicationRunner {

    private final StudentAccountResolver studentAccountResolver;

    @Override
    public void run(ApplicationArguments args) {
        studentAccountResolver.syncMissingProfiles();
    }
}

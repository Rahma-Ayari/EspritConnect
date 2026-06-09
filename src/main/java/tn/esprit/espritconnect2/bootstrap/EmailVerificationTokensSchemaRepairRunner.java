package tn.esprit.espritconnect2.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ensures {@code email_verification_tokens.id} supports UUID VARCHAR(36) keys.
 * Runs on every startup (independent of the one-shot users schema repair flag).
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class EmailVerificationTokensSchemaRepairRunner implements ApplicationRunner {

    private final UsersSchemaRepairService usersSchemaRepairService;

    @Override
    public void run(ApplicationArguments args) {
        usersSchemaRepairService.repairEmailVerificationTokensIfNeeded();
    }
}

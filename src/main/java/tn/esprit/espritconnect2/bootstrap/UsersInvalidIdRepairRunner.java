package tn.esprit.espritconnect2.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Safety net: re-run UUID schema repair after the context is up (no-op if already done).
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class UsersInvalidIdRepairRunner implements ApplicationRunner {

    private final UsersSchemaRepairService usersSchemaRepairService;

    @Override
    public void run(ApplicationArguments args) {
        usersSchemaRepairService.repairIfNeeded();
    }
}

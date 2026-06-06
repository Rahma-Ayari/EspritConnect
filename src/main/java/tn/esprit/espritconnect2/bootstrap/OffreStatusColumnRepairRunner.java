package tn.esprit.espritconnect2.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Converts MySQL {@code offre.statut_ofrre} from a legacy ENUM
 * (EN_ATTENTE, ACCEPTEE, REFUSEE) to VARCHAR so job statuses
 * (ACTIVE, DRAFT, ARCHIVED, CLOSED, PAUSED) can be stored.
 */
@Component
@Order(Integer.MAX_VALUE - 2)
@ConditionalOnProperty(prefix = "app.db", name = "repair-offre-status-column", havingValue = "true", matchIfMissing = true)
@Slf4j
public class OffreStatusColumnRepairRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public OffreStatusColumnRepairRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            if (url == null || !url.toLowerCase().contains("mysql")) {
                return;
            }
        } catch (Exception e) {
            log.debug("Skipping offre status column repair (could not read datasource URL): {}", e.getMessage());
            return;
        }

        try {
            jdbcTemplate.execute("ALTER TABLE offre MODIFY COLUMN statut_ofrre VARCHAR(50)");
            log.info("Ensured MySQL column offre.statut_ofrre uses VARCHAR(50)");
        } catch (Exception e) {
            log.warn("Could not ALTER offre.statut_ofrre to VARCHAR(50): {}", e.getMessage());
        }
    }
}

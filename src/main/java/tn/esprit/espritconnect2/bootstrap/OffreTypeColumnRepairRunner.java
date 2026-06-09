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
 * Converts MySQL {@code offre.type_offre} from a legacy ENUM
 * (CV, EMPLOI, PORTFOLIO, STAGE) to VARCHAR so all {@link tn.esprit.espritconnect2.Entitie.Type}
 * values (APPRENTISSAGE, PFE, …) can be stored.
 */
@Component
@Order(Integer.MAX_VALUE - 3)
@ConditionalOnProperty(prefix = "app.db", name = "repair-offre-type-column", havingValue = "true", matchIfMissing = true)
@Slf4j
public class OffreTypeColumnRepairRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public OffreTypeColumnRepairRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
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
            log.debug("Skipping offre type column repair (could not read datasource URL): {}", e.getMessage());
            return;
        }

        try {
            jdbcTemplate.execute("ALTER TABLE offre MODIFY COLUMN type_offre VARCHAR(50) NOT NULL");
            log.info("Ensured MySQL column offre.type_offre uses VARCHAR(50)");
        } catch (Exception e) {
            log.warn("Could not ALTER offre.type_offre to VARCHAR(50): {}", e.getMessage());
        }
    }
}

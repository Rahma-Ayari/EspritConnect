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
 * Widens {@code candidature.statut_candidature} so values like EN_ENTRETIEN fit
 * (legacy MySQL ENUM/VARCHAR(10) only allowed EN_ATTENTE, ACCEPTEE, REFUSEE).
 */
@Component
@Order(Integer.MAX_VALUE - 1)
@ConditionalOnProperty(prefix = "app.db", name = "repair-candidature-status-column", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CandidatureStatusColumnRepairRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public CandidatureStatusColumnRepairRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
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
            log.debug("Skipping candidature status column repair: {}", e.getMessage());
            return;
        }

        try {
            jdbcTemplate.execute("ALTER TABLE candidature MODIFY COLUMN statut_candidature VARCHAR(50)");
            log.info("Ensured MySQL column candidature.statut_candidature uses VARCHAR(50)");
        } catch (Exception e) {
            log.warn("Could not ALTER candidature.statut_candidature to VARCHAR(50): {}", e.getMessage());
        }
    }
}

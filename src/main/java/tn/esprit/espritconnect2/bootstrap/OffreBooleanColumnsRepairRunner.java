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

/** Ensures archive/pin boolean columns exist on MySQL {@code offre}. */
@Component
@Order(Integer.MAX_VALUE - 3)
@ConditionalOnProperty(prefix = "app.db", name = "repair-offre-boolean-columns", havingValue = "true", matchIfMissing = true)
@Slf4j
public class OffreBooleanColumnsRepairRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public OffreBooleanColumnsRepairRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
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
            log.debug("Skipping offre boolean column repair: {}", e.getMessage());
            return;
        }

        try {
            jdbcTemplate.execute("ALTER TABLE offre ADD COLUMN IF NOT EXISTS is_archived TINYINT(1) NOT NULL DEFAULT 0");
            log.info("Ensured MySQL column offre.is_archived exists");
        } catch (Exception e) {
            try {
                jdbcTemplate.execute("ALTER TABLE offre ADD COLUMN is_archived TINYINT(1) NOT NULL DEFAULT 0");
                log.info("Added MySQL column offre.is_archived");
            } catch (Exception ignored) {
                log.warn("Could not ensure offre.is_archived: {}", e.getMessage());
            }
        }

        try {
            jdbcTemplate.execute("ALTER TABLE offre ADD COLUMN IF NOT EXISTS is_pinned TINYINT(1) NOT NULL DEFAULT 0");
            log.info("Ensured MySQL column offre.is_pinned exists");
        } catch (Exception e) {
            try {
                jdbcTemplate.execute("ALTER TABLE offre ADD COLUMN is_pinned TINYINT(1) NOT NULL DEFAULT 0");
                log.info("Added MySQL column offre.is_pinned");
            } catch (Exception ignored) {
                log.warn("Could not ensure offre.is_pinned: {}", e.getMessage());
            }
        }
    }
}

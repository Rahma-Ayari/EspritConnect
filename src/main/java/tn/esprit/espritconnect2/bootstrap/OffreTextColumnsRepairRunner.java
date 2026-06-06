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

/** Expands MySQL {@code offre} text columns when ddl-update left them as VARCHAR(255). */
@Component
@Order(Integer.MAX_VALUE - 1)
@ConditionalOnProperty(prefix = "app.db", name = "repair-offre-text-columns", havingValue = "true", matchIfMissing = true)
@Slf4j
public class OffreTextColumnsRepairRunner implements ApplicationRunner {

    private static final String[] TEXT_COLUMNS = {
            "description",
            "responsibilities",
            "requirements",
            "benefits"
    };

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public OffreTextColumnsRepairRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
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
            log.debug("Skipping offre text column repair (could not read datasource URL): {}", e.getMessage());
            return;
        }

        for (String column : TEXT_COLUMNS) {
            try {
                jdbcTemplate.execute("ALTER TABLE offre MODIFY COLUMN " + column + " TEXT");
                log.info("Ensured MySQL column offre.{} uses TEXT", column);
            } catch (Exception e) {
                log.warn("Could not ALTER offre.{} to TEXT: {}", column, e.getMessage());
            }
        }
    }
}

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

/** Aligns MySQL {@code offre.id_offre} with JPA {@code GenerationType.IDENTITY} when ddl-update left the column non-auto-increment. */
@Component
@Order(Integer.MAX_VALUE)
@ConditionalOnProperty(prefix = "app.db", name = "repair-offre-id-autoincrement", havingValue = "true", matchIfMissing = true)
@Slf4j
public class OffreIdAutoIncrementRepairRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public OffreIdAutoIncrementRepairRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
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
            log.debug("Skipping offre id repair (could not read datasource URL): {}", e.getMessage());
            return;
        }
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE offre MODIFY COLUMN id_offre BIGINT NOT NULL AUTO_INCREMENT"
            );
            log.info("Ensured MySQL column offre.id_offre uses AUTO_INCREMENT");
        } catch (Exception e) {
            log.warn("Could not ALTER offre.id_offre for AUTO_INCREMENT: {}", e.getMessage());
        }
    }
}

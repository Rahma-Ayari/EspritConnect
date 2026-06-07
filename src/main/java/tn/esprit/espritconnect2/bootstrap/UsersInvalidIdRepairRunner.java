package tn.esprit.espritconnect2.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Removes user rows whose {@code id} cannot be mapped to {@link java.util.UUID}
 * (legacy numeric ids or truncated values after a partial schema migration).
 */
@Component
@Order(1)
@Slf4j
public class UsersInvalidIdRepairRunner implements ApplicationRunner {

    private static final String UUID_PATTERN =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public UsersInvalidIdRepairRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isMysql(dataSource)) {
            return;
        }
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            widenUserIdColumns();
            int removed = jdbcTemplate.update(
                    "DELETE FROM users WHERE id IS NULL "
                            + "OR CHAR_LENGTH(id) <> 36 "
                            + "OR id NOT REGEXP ?",
                    UUID_PATTERN
            );
            if (removed > 0) {
                log.info("Removed {} user row(s) with invalid UUID id", removed);
            }
        } catch (Exception e) {
            log.warn("Could not repair users schema: {}", e.getMessage());
        } finally {
            try {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    private void widenUserIdColumns() {
        var foreignKeys = jdbcTemplate.queryForList(
                "SELECT CONSTRAINT_NAME AS constraintName, TABLE_NAME AS tableName, COLUMN_NAME AS columnName "
                        + "FROM information_schema.KEY_COLUMN_USAGE "
                        + "WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND REFERENCED_TABLE_NAME = 'users' "
                        + "AND REFERENCED_COLUMN_NAME = 'id' "
                        + "AND CONSTRAINT_NAME <> 'PRIMARY'"
        );
        for (var row : foreignKeys) {
            String table = String.valueOf(row.get("tableName"));
            String constraint = String.valueOf(row.get("constraintName"));
            try {
                jdbcTemplate.execute("ALTER TABLE `" + table + "` DROP FOREIGN KEY `" + constraint + "`");
                log.info("Dropped foreign key {} on {}", constraint, table);
            } catch (Exception e) {
                log.debug("Could not drop FK {} on {}: {}", constraint, table, e.getMessage());
            }
        }
        for (var row : foreignKeys) {
            String table = String.valueOf(row.get("tableName"));
            String column = String.valueOf(row.get("columnName"));
            try {
                jdbcTemplate.execute(
                        "ALTER TABLE `" + table + "` MODIFY COLUMN `" + column + "` VARCHAR(36) NOT NULL"
                );
                log.info("Ensured {}.{} uses VARCHAR(36)", table, column);
            } catch (Exception e) {
                try {
                    jdbcTemplate.execute(
                            "ALTER TABLE `" + table + "` MODIFY COLUMN `" + column + "` VARCHAR(36) NULL"
                    );
                    log.info("Ensured {}.{} uses VARCHAR(36) (nullable)", table, column);
                } catch (Exception ignored) {
                    log.debug("Could not widen {}.{}: {}", table, column, e.getMessage());
                }
            }
        }
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN id VARCHAR(36) NOT NULL");
            log.info("Ensured users.id uses VARCHAR(36)");
        } catch (Exception e) {
            log.warn("Could not widen users.id: {}", e.getMessage());
        }
    }

    private static boolean isMysql(DataSource dataSource) {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            return url != null && url.toLowerCase().contains("mysql");
        } catch (Exception e) {
            return false;
        }
    }
}

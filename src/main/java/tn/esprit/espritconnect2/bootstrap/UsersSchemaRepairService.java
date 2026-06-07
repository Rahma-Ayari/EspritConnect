package tn.esprit.espritconnect2.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class UsersSchemaRepairService {

    static final String UUID_PATTERN =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private static final AtomicBoolean REPAIRED = new AtomicBoolean(false);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public UsersSchemaRepairService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    public void repairIfNeeded() {
        if (!REPAIRED.compareAndSet(false, true)) {
            return;
        }
        if (!isMysql(dataSource) || !tableExists("users")) {
            return;
        }
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            widenUserIdColumns();
            purgeInvalidUserReferences();
            int removed = jdbcTemplate.update(
                    "DELETE FROM users WHERE id IS NULL "
                            + "OR CHAR_LENGTH(CAST(id AS CHAR)) <> 36 "
                            + "OR CAST(id AS CHAR) NOT REGEXP ?",
                    UUID_PATTERN
            );
            if (removed > 0) {
                log.info("Removed {} user row(s) with invalid UUID id", removed);
            }
        } catch (Exception e) {
            log.error("Could not repair users schema: {}", e.getMessage(), e);
            throw new IllegalStateException(
                    "users.id and user_id foreign keys must be VARCHAR(36) for UUID keys.",
                    e
            );
        } finally {
            try {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    private void purgeInvalidUserReferences() {
        for (Map<String, Object> row : findAllUserIdColumns()) {
            String table = String.valueOf(row.get("tableName"));
            if ("users".equals(table)) {
                continue;
            }
            if (!tableExists(table)) {
                continue;
            }
            try {
                int invalid = jdbcTemplate.update(
                        "DELETE FROM `" + table + "` WHERE user_id IS NULL "
                                + "OR CHAR_LENGTH(CAST(user_id AS CHAR)) <> 36 "
                                + "OR CAST(user_id AS CHAR) NOT REGEXP ?",
                        UUID_PATTERN
                );
                int orphans = jdbcTemplate.update(
                        "DELETE FROM `" + table + "` WHERE user_id NOT IN (SELECT id FROM users)"
                );
                if (invalid + orphans > 0) {
                    log.info("Purged {} invalid and {} orphan row(s) from {}", invalid, orphans, table);
                }
            } catch (Exception e) {
                log.warn("Could not purge {}: {}", table, e.getMessage());
            }
        }
    }

    private void widenUserIdColumns() {
        List<Map<String, Object>> foreignKeys = findForeignKeysReferencingUsers();
        for (Map<String, Object> row : foreignKeys) {
            dropForeignKey(row);
        }

        if (!isVarchar36("users", "id")) {
            log.info("Migrating users.id to VARCHAR(36)");
            alterUsersIdColumn();
        }

        for (Map<String, Object> row : findAllUserIdColumns()) {
            String table = String.valueOf(row.get("tableName"));
            if ("users".equals(table) || !tableExists(table)) {
                continue;
            }
            if (!isVarchar36(table, "user_id")) {
                log.info("Migrating {}.user_id to VARCHAR(36)", table);
                widenUserIdColumn(table);
            }
        }
    }

    private List<Map<String, Object>> findAllUserIdColumns() {
        return jdbcTemplate.queryForList(
                "SELECT TABLE_NAME AS tableName "
                        + "FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND COLUMN_NAME = 'user_id'"
        );
    }

    private boolean isVarchar36(String table, String column) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT DATA_TYPE AS dataType, CHARACTER_MAXIMUM_LENGTH AS charLength "
                        + "FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                table, column
        );
        if (rows.isEmpty()) {
            return true;
        }
        Map<String, Object> meta = rows.get(0);
        String dataType = String.valueOf(meta.get("dataType"));
        Object charLength = meta.get("charLength");
        return "varchar".equalsIgnoreCase(dataType)
                && charLength instanceof Number length
                && length.longValue() >= 36;
    }

    private void widenUserIdColumn(String table) {
        boolean nullable = isColumnNullable(table, "user_id");
        if (!modifyColumnToVarchar36(table, "user_id", nullable)) {
            modifyColumnToVarchar36(table, "user_id", true);
        }
    }

    private boolean isColumnNullable(String table, String column) {
        String nullable = jdbcTemplate.queryForObject(
                "SELECT IS_NULLABLE FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                String.class,
                table, column
        );
        return "YES".equalsIgnoreCase(nullable);
    }

    private List<Map<String, Object>> findForeignKeysReferencingUsers() {
        return jdbcTemplate.queryForList(
                "SELECT CONSTRAINT_NAME AS constraintName, TABLE_NAME AS tableName, COLUMN_NAME AS columnName "
                        + "FROM information_schema.KEY_COLUMN_USAGE "
                        + "WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND REFERENCED_TABLE_NAME = 'users' "
                        + "AND REFERENCED_COLUMN_NAME = 'id' "
                        + "AND CONSTRAINT_NAME <> 'PRIMARY'"
        );
    }

    private void dropForeignKey(Map<String, Object> row) {
        String table = String.valueOf(row.get("tableName"));
        String constraint = String.valueOf(row.get("constraintName"));
        try {
            jdbcTemplate.execute("ALTER TABLE `" + table + "` DROP FOREIGN KEY `" + constraint + "`");
            log.info("Dropped foreign key {} on {}", constraint, table);
        } catch (Exception e) {
            log.debug("Could not drop FK {} on {}: {}", constraint, table, e.getMessage());
        }
    }

    private boolean modifyColumnToVarchar36(String table, String column, boolean nullable) {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE `" + table + "` MODIFY COLUMN `" + column + "` VARCHAR(36) "
                            + (nullable ? "NULL" : "NOT NULL")
            );
            log.info("Ensured {}.{} uses VARCHAR(36){}", table, column, nullable ? " (nullable)" : "");
            return true;
        } catch (Exception e) {
            log.debug("Could not widen {}.{}: {}", table, column, e.getMessage());
            return false;
        }
    }

    private void alterUsersIdColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN id VARCHAR(36) NOT NULL");
            log.info("Ensured users.id uses VARCHAR(36)");
            return;
        } catch (Exception e) {
            log.info("Simple ALTER on users.id failed ({}), rebuilding primary key", e.getMessage());
        }

        jdbcTemplate.execute("ALTER TABLE users DROP PRIMARY KEY");
        jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN id VARCHAR(36) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE users ADD PRIMARY KEY (id)");
        log.info("Rebuilt users.id as VARCHAR(36) primary key");
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
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

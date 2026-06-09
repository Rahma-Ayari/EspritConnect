package tn.esprit.espritconnect2.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class UsersSchemaRepairService {

    static final String UUID_PATTERN =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private static final String USERS_ID_TEMP = "_ec_users_id_v36";

    private static final AtomicBoolean REPAIRED = new AtomicBoolean(false);

    private static final AtomicBoolean BIN_TO_UUID_AVAILABLE = new AtomicBoolean(true);

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
            repairEmailVerificationTokensIfNeeded();
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
            REPAIRED.set(false);
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

    public void repairEmailVerificationTokensIfNeeded() {
        if (!isMysql(dataSource)) {
            return;
        }
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            ensureUuidPrimaryKeyColumn("email_verification_tokens");
        } finally {
            try {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    private void ensureUuidPrimaryKeyColumn(String table) {
        if (!tableExists(table) || !columnExists(table, "id") || isVarchar36(table, "id")) {
            return;
        }

        log.info("Migrating {}.id to VARCHAR(36) for UUID primary keys", table);

        if (isBinaryUuidStorage(table, "id")) {
            migrateStandaloneUuidIdFromBinary(table);
            return;
        }

        jdbcTemplate.update("DELETE FROM `" + table + "`");
        dropPrimaryKeyIfExists(table);
        if (!modifyColumnToVarchar36(table, "id", false)) {
            dropPrimaryKeyIfExists(table);
            jdbcTemplate.execute(
                    "ALTER TABLE `" + table + "` MODIFY COLUMN id VARCHAR(36) NOT NULL"
            );
        }
        addPrimaryKeyIfMissing(table, "id");
        log.info("Rebuilt {}.id as VARCHAR(36)", table);
    }

    private void migrateStandaloneUuidIdFromBinary(String table) {
        String tempCol = "_ec_id_v36";
        if (!columnExists(table, tempCol)) {
            jdbcTemplate.execute(
                    "ALTER TABLE `" + table + "` ADD COLUMN `" + tempCol + "` VARCHAR(36) NULL"
            );
        }
        populateVarcharUuidFromBinary(table, "id", tempCol);

        jdbcTemplate.update("DELETE FROM `" + table + "` WHERE `" + tempCol + "` IS NULL");
        dropPrimaryKeyIfExists(table);
        jdbcTemplate.execute("ALTER TABLE `" + table + "` DROP COLUMN id");
        jdbcTemplate.execute(
                "ALTER TABLE `" + table + "` CHANGE `" + tempCol + "` id VARCHAR(36) NOT NULL"
        );
        addPrimaryKeyIfMissing(table, "id");
        log.info("Migrated {}.id from binary UUID to VARCHAR(36)", table);
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
            if (isBinaryUuidStorage("users", "id")) {
                log.info("Migrating users.id from binary UUID storage to VARCHAR(36)");
                migrateUsersIdFromBinary();
            } else {
                log.info("Migrating users.id to VARCHAR(36)");
                alterUsersIdColumn();
            }
        } else if (columnExists("users", USERS_ID_TEMP)) {
            jdbcTemplate.execute("ALTER TABLE users DROP COLUMN `" + USERS_ID_TEMP + "`");
        }

        for (Map<String, Object> row : findAllUserIdColumns()) {
            String table = String.valueOf(row.get("tableName"));
            if ("users".equals(table) || !tableExists(table)) {
                continue;
            }
            if (isBinaryUuidStorage(table, "user_id")) {
                log.info("Migrating {}.user_id from binary UUID storage to VARCHAR(36)", table);
                migrateUserIdColumnFromBinary(table);
            } else if (!isVarchar36(table, "user_id")) {
                log.info("Migrating {}.user_id to VARCHAR(36)", table);
                widenUserIdColumn(table);
            }
        }
    }

    private void migrateUsersIdFromBinary() {
        if (isVarchar36("users", "id") && !columnExists("users", USERS_ID_TEMP)) {
            return;
        }

        if (columnExists("users", "id")) {
            if (!columnExists("users", USERS_ID_TEMP)) {
                jdbcTemplate.execute(
                        "ALTER TABLE users ADD COLUMN `" + USERS_ID_TEMP + "` VARCHAR(36) NULL"
                );
            }
            populateVarcharUuidFromBinary("users", "id", USERS_ID_TEMP);

            for (Map<String, Object> row : findAllUserIdColumns()) {
                String table = String.valueOf(row.get("tableName"));
                if ("users".equals(table) || !tableExists(table) || !columnExists(table, "user_id")) {
                    continue;
                }
                if (!isBinaryUuidStorage(table, "user_id")) {
                    continue;
                }
                migrateUserIdColumnFromBinaryUsingUsersTemp(table);
            }

            dropPrimaryKeyIfExists("users");
            jdbcTemplate.execute("ALTER TABLE users DROP COLUMN id");
        }

        if (!columnExists("users", USERS_ID_TEMP)) {
            throw new IllegalStateException(
                    "users.id migration incomplete: temporary UUID column " + USERS_ID_TEMP + " is missing"
            );
        }

        if (!columnExists("users", "id")) {
            jdbcTemplate.execute(
                    "ALTER TABLE users CHANGE `" + USERS_ID_TEMP + "` id VARCHAR(36) NOT NULL"
            );
        } else if (columnExists("users", USERS_ID_TEMP)) {
            jdbcTemplate.update(
                    "UPDATE users SET id = `" + USERS_ID_TEMP + "` "
                            + "WHERE `" + USERS_ID_TEMP + "` IS NOT NULL "
                            + "AND (id IS NULL OR CAST(id AS CHAR) <> `" + USERS_ID_TEMP + "`)"
            );
            jdbcTemplate.execute("ALTER TABLE users DROP COLUMN `" + USERS_ID_TEMP + "`");
        }

        addPrimaryKeyIfMissing("users", "id");
        log.info("Rebuilt users.id from binary UUID to VARCHAR(36)");
    }

    private void migrateUserIdColumnFromBinary(String table) {
        if (tableExists("users") && columnExists("users", "id") && isVarchar36("users", "id")) {
            migrateUserIdColumnFromBinaryUsingUsersTemp(table);
            return;
        }

        String tempCol = "_ec_user_id_v36";
        if (!columnExists(table, tempCol)) {
            jdbcTemplate.execute(
                    "ALTER TABLE `" + table + "` ADD COLUMN `" + tempCol + "` VARCHAR(36) NULL"
            );
        }
        populateVarcharUuidFromBinary(table, "user_id", tempCol);

        boolean nullable = isColumnNullable(table, "user_id");
        jdbcTemplate.update("DELETE FROM `" + table + "` WHERE `" + tempCol + "` IS NULL");
        jdbcTemplate.execute("ALTER TABLE `" + table + "` DROP COLUMN user_id");
        jdbcTemplate.execute(
                "ALTER TABLE `" + table + "` CHANGE `" + tempCol + "` user_id VARCHAR(36) "
                        + (nullable ? "NULL" : "NOT NULL")
        );
        log.info("Migrated {}.user_id from binary UUID to VARCHAR(36)", table);
    }

    private void migrateUserIdColumnFromBinaryUsingUsersTemp(String table) {
        String tempCol = "_ec_user_id_v36";
        if (!columnExists(table, tempCol)) {
            jdbcTemplate.execute(
                    "ALTER TABLE `" + table + "` ADD COLUMN `" + tempCol + "` VARCHAR(36) NULL"
            );
        }

        if (columnExists("users", USERS_ID_TEMP)) {
            jdbcTemplate.update(
                    "UPDATE `" + table + "` t "
                            + "INNER JOIN users u ON t.user_id = u.id "
                            + "SET t.`" + tempCol + "` = u.`" + USERS_ID_TEMP + "` "
                            + "WHERE t.`" + tempCol + "` IS NULL"
            );
        } else if (columnExists("users", "id") && isVarchar36("users", "id")) {
            jdbcTemplate.update(
                    "UPDATE `" + table + "` t "
                            + "INNER JOIN users u ON t.user_id = u.id "
                            + "SET t.`" + tempCol + "` = u.id "
                            + "WHERE t.`" + tempCol + "` IS NULL"
            );
        } else {
            populateVarcharUuidFromBinary(table, "user_id", tempCol);
        }

        boolean nullable = isColumnNullable(table, "user_id");
        jdbcTemplate.update("DELETE FROM `" + table + "` WHERE `" + tempCol + "` IS NULL");
        jdbcTemplate.execute("ALTER TABLE `" + table + "` DROP COLUMN user_id");
        jdbcTemplate.execute(
                "ALTER TABLE `" + table + "` CHANGE `" + tempCol + "` user_id VARCHAR(36) "
                        + (nullable ? "NULL" : "NOT NULL")
        );
        log.info("Migrated {}.user_id using users UUID mapping", table);
    }

    private void populateVarcharUuidFromBinary(String table, String binaryColumn, String varcharColumn) {
        if (isBinToUuidAvailable()) {
            tryPopulateWithBinToUuid(table, binaryColumn, varcharColumn, false);
            tryPopulateWithBinToUuid(table, binaryColumn, varcharColumn, true);
        }

        jdbcTemplate.update(
                "UPDATE `" + table + "` SET `" + varcharColumn + "` = LOWER(CONCAT("
                        + "SUBSTR(HEX(`" + binaryColumn + "`), 1, 8), '-', "
                        + "SUBSTR(HEX(`" + binaryColumn + "`), 9, 4), '-', "
                        + "SUBSTR(HEX(`" + binaryColumn + "`), 13, 4), '-', "
                        + "SUBSTR(HEX(`" + binaryColumn + "`), 17, 4), '-', "
                        + "SUBSTR(HEX(`" + binaryColumn + "`), 21, 12)"
                        + ")) WHERE `" + binaryColumn + "` IS NOT NULL AND `" + varcharColumn + "` IS NULL"
        );

        populateRemainingBinaryUuidsInJava(table, binaryColumn, varcharColumn);
    }

    private boolean isBinToUuidAvailable() {
        if (!BIN_TO_UUID_AVAILABLE.get()) {
            return false;
        }
        try {
            jdbcTemplate.queryForObject(
                    "SELECT BIN_TO_UUID(UNHEX('00112233445566778899AABBCCDDEEFF'))",
                    String.class
            );
            return true;
        } catch (Exception e) {
            BIN_TO_UUID_AVAILABLE.set(false);
            log.info("MySQL BIN_TO_UUID is unavailable; using HEX/Java UUID conversion instead");
            return false;
        }
    }

    private void tryPopulateWithBinToUuid(String table, String binaryColumn, String varcharColumn, boolean swapFlag) {
        String expression = swapFlag
                ? "LOWER(BIN_TO_UUID(`" + binaryColumn + "`, 1))"
                : "LOWER(BIN_TO_UUID(`" + binaryColumn + "`))";
        try {
            jdbcTemplate.update(
                    "UPDATE `" + table + "` SET `" + varcharColumn + "` = " + expression + " "
                            + "WHERE `" + binaryColumn + "` IS NOT NULL AND `" + varcharColumn + "` IS NULL"
            );
        } catch (Exception e) {
            BIN_TO_UUID_AVAILABLE.set(false);
            log.debug("BIN_TO_UUID conversion failed for {}.{}: {}", table, binaryColumn, e.getMessage());
        }
    }

    private void populateRemainingBinaryUuidsInJava(String table, String binaryColumn, String varcharColumn) {
        List<byte[]> pending = jdbcTemplate.query(
                "SELECT `" + binaryColumn + "` AS binVal FROM `" + table + "` "
                        + "WHERE `" + binaryColumn + "` IS NOT NULL AND `" + varcharColumn + "` IS NULL",
                (rs, rowNum) -> rs.getBytes("binVal")
        );
        for (byte[] bytes : pending) {
            if (bytes == null || bytes.length != 16) {
                continue;
            }
            String uuid = bytesToUuidString(bytes).toLowerCase();
            jdbcTemplate.update(
                    "UPDATE `" + table + "` SET `" + varcharColumn + "` = ? "
                            + "WHERE `" + binaryColumn + "` = ? AND `" + varcharColumn + "` IS NULL",
                    uuid, bytes
            );
        }
    }

    private static String bytesToUuidString(byte[] bytes) {
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xffL);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xffL);
        }
        return new UUID(msb, lsb).toString();
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

    private boolean isBinaryUuidStorage(String table, String column) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT DATA_TYPE AS dataType, CHARACTER_MAXIMUM_LENGTH AS charLength "
                        + "FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                table, column
        );
        if (rows.isEmpty()) {
            return false;
        }
        Map<String, Object> meta = rows.get(0);
        String dataType = String.valueOf(meta.get("dataType")).toLowerCase();
        Object charLength = meta.get("charLength");
        if ("binary".equals(dataType) || "varbinary".equals(dataType)) {
            return !(charLength instanceof Number length) || length.longValue() == 16;
        }
        return "blob".equals(dataType) || "tinyblob".equals(dataType);
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                table, column
        );
        return count != null && count > 0;
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

    private boolean hasPrimaryKey(String table) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS "
                        + "WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = ? AND CONSTRAINT_TYPE = 'PRIMARY KEY'",
                Integer.class,
                table
        );
        return count != null && count > 0;
    }

    private void dropPrimaryKeyIfExists(String table) {
        if (hasPrimaryKey(table)) {
            jdbcTemplate.execute("ALTER TABLE `" + table + "` DROP PRIMARY KEY");
        }
    }

    private void addPrimaryKeyIfMissing(String table, String column) {
        if (!hasPrimaryKey(table)) {
            jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD PRIMARY KEY (`" + column + "`)");
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

        try {
            dropPrimaryKeyIfExists("users");
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN id VARCHAR(36) NOT NULL");
            addPrimaryKeyIfMissing("users", "id");
            log.info("Rebuilt users.id as VARCHAR(36) primary key");
        } catch (Exception e) {
            if (isBinaryUuidStorage("users", "id")) {
                migrateUsersIdFromBinary();
                return;
            }
            throw e;
        }
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

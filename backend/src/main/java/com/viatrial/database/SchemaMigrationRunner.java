package com.viatrial.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 版本化 schema 迁移（审计项 D-3）。
 *
 * <p>原先全部建表语句都是 {@code CREATE TABLE IF NOT EXISTS}，已存在的库不会获得任何
 * 结构变更，版本升级时会直接 500。这里引入一个极简迁移机制：</p>
 *
 * <ul>
 *   <li>{@code schema_version} 表记录已应用的最高版本；全新库与历史库都从 0 开始，
 *       {@code V1} 即 {@code schema.sql} 基线本身，历史库因基线幂等而安全跳过。</li>
 *   <li>{@code classpath:db/migration/V<n>__<描述>.sql} 按版本号顺序应用，每个文件在
 *       独立事务中执行并记录版本；脚本必须幂等（{@code IF EXISTS}/{@code IF NOT EXISTS}）。</li>
 *   <li>迁移失败时事务回滚、版本号不变，启动中断并打印明确日志，避免半迁移状态。</li>
 * </ul>
 */
@Component
public class SchemaMigrationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrationRunner.class);

    private static final List<Migration> MIGRATIONS = List.of(
            new Migration(2, "db/migration/V2__index_maintenance.sql"));
    private static final int BASELINE_VERSION = 1;

    public void migrate(Connection connection) throws SQLException {
        createVersionTable(connection);
        int currentVersion = currentVersion(connection);

        for (Migration migration : MIGRATIONS) {
            if (migration.version() <= currentVersion) {
                continue;
            }
            apply(connection, migration);
        }
    }

    private void createVersionTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS schema_version (
                        version INTEGER PRIMARY KEY,
                        description TEXT NOT NULL,
                        applied_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    private int currentVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COALESCE(MAX(version), 0) FROM schema_version")) {
            int recorded = resultSet.next() ? resultSet.getInt(1) : 0;
            return Math.max(recorded, BASELINE_VERSION);
        }
    }

    private void apply(Connection connection, Migration migration) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(migration.resource()));

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO schema_version (version, description) VALUES (?, ?)")) {
                statement.setInt(1, migration.version());
                statement.setString(2, migration.resource());
                statement.executeUpdate();
            }

            connection.commit();
            log.info("Applied schema migration V{} ({})", migration.version(), migration.resource());
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            log.error("Schema migration V{} ({}) failed and was rolled back.",
                    migration.version(), migration.resource(), e);
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private record Migration(int version, String resource) {
    }
}

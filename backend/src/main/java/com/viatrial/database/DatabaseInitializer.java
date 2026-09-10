package com.viatrial.database;

import com.viatrial.config.DataDirectoryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库初始化：建表基线 + 版本化增量迁移。
 *
 * <p>审计项 D-1：schema 只保留 {@code classpath:schema.sql} 一份真相源，原先内容相同的
 * {@code sql/init.sql} 副本已删除，避免“改到备份文件”式的静默失效。</p>
 *
 * <p>审计项 D-8：改用 Spring {@code ScriptUtils} 解析脚本，替代原先按 {@code ";"} 裸切分的
 * 脆弱实现（字符串字面量/CHECK/触发器体内的分号会被错误拆分）。</p>
 */
@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    static final String SCHEMA_RESOURCE = "schema.sql";

    private final DataSource dataSource;

    private final DataDirectoryResolver dataDirectoryResolver;

    private final SchemaMigrationRunner schemaMigrationRunner;

    public DatabaseInitializer(DataSource dataSource,
                               DataDirectoryResolver dataDirectoryResolver,
                               SchemaMigrationRunner schemaMigrationRunner) {
        this.dataSource = dataSource;
        this.dataDirectoryResolver = dataDirectoryResolver;
        this.schemaMigrationRunner = schemaMigrationRunner;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            enableForeignKeys(connection);
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(SCHEMA_RESOURCE));
            schemaMigrationRunner.migrate(connection);
            log.info("Database ready: {}", dataDirectoryResolver.getDatabaseUrl());
        }
    }

    /**
     * SQLite 的外键开关是连接级设置且默认关闭（审计项 D-5）。所有连接都必须显式开启，
     * 否则 {@code RESTRICT}/{@code CASCADE} 会被静默忽略。
     */
    static void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        try (Statement statement = connection.createStatement();
             var resultSet = statement.executeQuery("PRAGMA foreign_keys")) {
            if (resultSet.next() && resultSet.getInt(1) != 1) {
                throw new IllegalStateException("SQLite foreign key enforcement could not be enabled.");
            }
        }
    }
}

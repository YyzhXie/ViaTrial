package com.viatrial.database;

import com.viatrial.config.DatabaseConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class DatabaseInitializer {

    private static final String SCHEMA_PATH = "/sql/init.sql";

    private DatabaseInitializer() {
    }

    public static void initialize() {
        createDataDirectory();
        executeSchemaSql();
    }

    private static void createDataDirectory() {
        try {
            Files.createDirectories(Path.of(DatabaseConfig.DATA_DIR));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create data directory: " + DatabaseConfig.DATA_DIR, e);
        }
    }

    private static void executeSchemaSql() {
        String sql = readSchemaSql();

        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");

            String[] statements = sql.split(";");

            for (String statementSql : statements) {
                String trimmedSql = statementSql.trim();
                if (!trimmedSql.isEmpty()) {
                    statement.execute(trimmedSql);
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database schema.", e);
        }
    }

    private static String readSchemaSql() {
        try (InputStream inputStream = DatabaseInitializer.class.getResourceAsStream(SCHEMA_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Schema file not found: " + SCHEMA_PATH);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read schema file: " + SCHEMA_PATH, e);
        }
    }
}

package com.viatrial.database;

import com.viatrial.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private DatabaseManager() {

    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DatabaseConfig.DATABASE_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return connection;
    }
}

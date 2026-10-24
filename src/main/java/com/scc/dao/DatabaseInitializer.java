package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING'
                );
                """;

        // Add the rest of your proposal's tables below (with FOREIGN KEY clauses), e.g.:
        // String queueTable = "CREATE TABLE IF NOT EXISTS ...";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(usersTable);
            // stmt.execute(queueTable);
            System.out.println("-> Database initialized.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
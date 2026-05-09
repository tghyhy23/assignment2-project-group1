package com.group01.asm2.db;

import com.group01.asm2.configs.DatabaseConfig;

import java.sql.Connection;
import java.sql.Statement;

public final class PostgreSQLInitializer {

    private PostgreSQLInitializer() {
    }

    public static void initSchema() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            createPersonsTable(stmt);
            createIndexes(stmt);

            System.out.println("Database schema initialized successfully.");

        } catch (Exception e) {
            throw new RuntimeException("Database schema initialization failed: " + e.getMessage(), e);
        }
    }

    private static void createPersonsTable(Statement stmt) throws Exception {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS persons (
                id SERIAL PRIMARY KEY,

                full_name VARCHAR(120) NOT NULL,
                date_of_birth DATE,

                email VARCHAR(255) NOT NULL,
                phone VARCHAR(30),
                username VARCHAR(80) NOT NULL,

                password_hash TEXT NOT NULL,

                role VARCHAR(40) NOT NULL CHECK (
                    role IN (
                        'BUYER',
                        'SELLER',
                        'AUCTION_ADMINISTRATOR',
                        'SYSTEM_ADMINISTRATOR'
                    )
                ),

                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                balance NUMERIC(12, 2) NOT NULL DEFAULT 0,
                rating DOUBLE PRECISION NOT NULL DEFAULT 0,
                completed_sales_count INTEGER NOT NULL DEFAULT 0,

                CHECK (balance >= 0),
                CHECK (rating >= 0),
                CHECK (completed_sales_count >= 0)
            )
        """);
    }

    private static void createIndexes(Statement stmt) throws Exception {
        stmt.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_persons_email_lower
            ON persons (LOWER(email))
        """);

        stmt.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_persons_username_lower
            ON persons (LOWER(username))
        """);
    }
}
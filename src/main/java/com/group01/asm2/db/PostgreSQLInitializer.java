package com.group01.asm2.db;

import com.group01.asm2.configs.DatabaseConfig;
import java.sql.Connection;
import java.sql.Statement;

public class PostgreSQLInitializer {

    public static void init() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    username TEXT NOT NULL,
                    balance DOUBLE PRECISION DEFAULT 0
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS items (
                    id SERIAL PRIMARY KEY,
                    title TEXT,
                    price DOUBLE PRECISION
                )
            """);

            System.out.println("✅ Tables created");

        } catch (Exception e) {
            throw new RuntimeException("Init failed: " + e.getMessage());
        }
    }
}
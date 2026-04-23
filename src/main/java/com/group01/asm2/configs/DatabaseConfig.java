package com.group01.asm2.configs;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConfig {

    public static Connection getConnection() {
        try {
            String url = AppConfig.get("db.url");
            String username = AppConfig.get("db.username");
            String password = AppConfig.get("db.password");

            System.out.println("DB URL: " + url);
            System.out.println("DB Username: " + username);
            System.out.println("Trying to connect to database...");

            Connection connection = DriverManager.getConnection(url, username, password);

            System.out.println("Database connected successfully");

            return connection;
        } catch (Exception e) {
            System.out.println("Database connection failed");
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
    }
}
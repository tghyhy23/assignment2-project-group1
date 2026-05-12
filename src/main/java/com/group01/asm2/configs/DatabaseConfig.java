package com.group01.asm2.configs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

public final class DatabaseConfig {
    private static final HikariDataSource dataSource;

    static {
        try {
            String url = AppConfig.get("db.url");
            String username = AppConfig.get("db.username");
            String password = AppConfig.get("db.password");

            System.out.println("DB URL: " + url);
            System.out.println("DB Username: " + username);

            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);

            // Small desktop app: keep pool small.
            config.setMaximumPoolSize(3);

            // Important for Neon/serverless DB:
            // Do not force Hikari to keep idle connections alive at startup.
            config.setMinimumIdle(0);

            // Give Neon more time to wake/connect.
            config.setConnectionTimeout(60000);

            // Validation timeout should be shorter than connection timeout.
            config.setValidationTimeout(5000);

            // Important:
            // Do not fail the whole app immediately if the first connection cannot be created.
            config.setInitializationFailTimeout(-1);

            // PostgreSQL driver-level timeout settings.
            config.addDataSourceProperty("connectTimeout", "30");
            config.addDataSourceProperty("socketTimeout", "60");
            config.addDataSourceProperty("tcpKeepAlive", "true");

            config.setPoolName("BidBlitzPool");

            dataSource = new HikariDataSource(config);

            System.out.println("Database connection pool initialized successfully.");

        } catch (Exception e) {
            throw new RuntimeException("Database connection pool initialization failed: " + e.getMessage(), e);
        }
    }

    private DatabaseConfig() {
    }

    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
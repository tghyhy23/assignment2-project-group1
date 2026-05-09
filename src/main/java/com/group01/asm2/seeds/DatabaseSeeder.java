package com.group01.asm2.seeds;

import com.group01.asm2.configs.DatabaseConfig;

import java.sql.Connection;
import java.util.List;

public final class DatabaseSeeder {

    private DatabaseSeeder() {
    }

    public static void seedAll() {
        List<Seeder> seeders = List.of(
            new PersonSeed()
            // Later:
            // new AuctionSeed(),
            // new ItemSeed(),
            // new BidSeed(),
            // new TransactionSeed()
        );

        Connection conn = null;

        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            for (Seeder seeder : seeders) {
                seeder.seed(conn);
            }

            conn.commit();
            System.out.println("Database seed completed successfully.");

        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Database seed failed: " + e.getMessage(), e);

        } finally {
            close(conn);
        }
    }

    private static void rollback(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.rollback();
        } catch (Exception e) {
            System.out.println("Rollback failed: " + e.getMessage());
        }
    }

    private static void close(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (Exception e) {
            System.out.println("Connection close failed: " + e.getMessage());
        }
    }
}
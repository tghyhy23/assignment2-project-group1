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

            // 1. Create parent/core tables first
            createPersonsTable(stmt);
            createCategoriesTable(stmt);

            // 2. Create auction domain tables
            createItemsTable(stmt);
            createItemImagesTable(stmt);
            createAuctionsTable(stmt);
            createBidsTable(stmt);
            addAuctionCurrentHighestBidForeignKey(stmt);

            // 3. Create user feature tables
            createAuctionWatchlistsTable(stmt);

            // 4. Create payment/audit tables
            createPaymentsTable(stmt);
            createActivityLogsTable(stmt);

            // 5. Create indexes
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

                password TEXT NOT NULL,

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

    private static void createCategoriesTable(Statement stmt) throws Exception {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS categories (
                id SERIAL PRIMARY KEY,

                name VARCHAR(80) NOT NULL,
                description VARCHAR(500),
                commission_rate NUMERIC(5, 2) NOT NULL,

                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                CHECK (commission_rate >= 0),
                CHECK (commission_rate <= 100)
            )
        """);
    }

    private static void createItemsTable(Statement stmt) throws Exception {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS items (
                id SERIAL PRIMARY KEY,

                title VARCHAR(120) NOT NULL,
                description TEXT NOT NULL,

                category_id INTEGER NOT NULL REFERENCES categories(id),
                seller_id INTEGER NOT NULL REFERENCES persons(id),

                starting_price NUMERIC(12, 2) NOT NULL,
                reserve_price NUMERIC(12, 2),

                condition VARCHAR(30) NOT NULL CHECK (
                    condition IN (
                        'NEW',
                        'USED',
                        'REFURBISHED'
                    )
                ),

                status VARCHAR(30) NOT NULL CHECK (
                    status IN (
                        'ACTIVE',
                        'REMOVED',
                        'CANCELLED'
                    )
                ),

                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                CHECK (starting_price > 0),
                CHECK (reserve_price IS NULL OR reserve_price >= 0),
                CHECK (reserve_price IS NULL OR reserve_price >= starting_price)
            )
        """);
    }

    private static void createItemImagesTable(Statement stmt) throws Exception {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS item_images (
                id SERIAL PRIMARY KEY,

                item_id INTEGER NOT NULL REFERENCES items(id) ON DELETE CASCADE,

                image_url TEXT NOT NULL,
                display_order INTEGER NOT NULL DEFAULT 0,

                CHECK (display_order >= 0)
            )
        """);
    }

    private static void createAuctionsTable(Statement stmt) throws Exception {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS auctions (
                id SERIAL PRIMARY KEY,

                item_id INTEGER NOT NULL UNIQUE REFERENCES items(id) ON DELETE CASCADE,

                status VARCHAR(30) NOT NULL CHECK (
                    status IN (
                        'ACTIVE',
                        'ENDED',
                        'SOLD',
                        'UNSOLD',
                        'CANCELLED'
                    )
                ),

                current_highest_bid_id INTEGER,
                winner_id INTEGER REFERENCES persons(id),
                final_sale_price NUMERIC(12, 2),

                start_date_time TIMESTAMP NOT NULL,
                end_date_time TIMESTAMP NOT NULL,

                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                recommended BOOLEAN NOT NULL DEFAULT FALSE,

                CHECK (end_date_time > start_date_time),
                CHECK (final_sale_price IS NULL OR final_sale_price >= 0)
            )
        """);
    }

    private static void createBidsTable(Statement stmt) throws Exception {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS bids (
                id SERIAL PRIMARY KEY,

                auction_id INTEGER NOT NULL REFERENCES auctions(id) ON DELETE CASCADE,
                bidder_id INTEGER NOT NULL REFERENCES persons(id),

                amount NUMERIC(12, 2) NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                CHECK (amount > 0)
            )
        """);
    }

    private static void addAuctionCurrentHighestBidForeignKey(Statement stmt) throws Exception {
        stmt.execute("""
            DO $$
            BEGIN
                IF NOT EXISTS (
                    SELECT 1
                    FROM information_schema.table_constraints
                    WHERE constraint_name = 'fk_auctions_current_highest_bid'
                      AND table_name = 'auctions'
                ) THEN
                    ALTER TABLE auctions
                    ADD CONSTRAINT fk_auctions_current_highest_bid
                    FOREIGN KEY (current_highest_bid_id)
                    REFERENCES bids(id)
                    ON DELETE SET NULL;
                END IF;
            END $$;
        """);
    }

    private static void createAuctionWatchlistsTable(Statement stmt) throws Exception {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS auction_watchlists (
                id SERIAL PRIMARY KEY,

                user_id INTEGER NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
                auction_id INTEGER NOT NULL REFERENCES auctions(id) ON DELETE CASCADE,

                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                CONSTRAINT unique_user_auction_watch UNIQUE (user_id, auction_id)
            )
        """);
    }

    private static void createPaymentsTable(Statement stmt) throws Exception {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS payments (
                id SERIAL PRIMARY KEY,

                auction_id INTEGER NOT NULL UNIQUE REFERENCES auctions(id),
                buyer_id INTEGER NOT NULL REFERENCES persons(id),
                seller_id INTEGER NOT NULL REFERENCES persons(id),

                total_amount NUMERIC(12, 2) NOT NULL,
                commission_amount NUMERIC(12, 2) NOT NULL,
                seller_payout NUMERIC(12, 2) NOT NULL,

                status VARCHAR(30) NOT NULL CHECK (
                    status IN (
                        'PENDING',
                        'COMPLETED',
                        'FAILED',
                        'CANCELLED'
                    )
                ),

                payment_date_time TIMESTAMP,

                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                CHECK (total_amount >= 0),
                CHECK (commission_amount >= 0),
                CHECK (seller_payout >= 0),
                CHECK (total_amount >= commission_amount)
            )
        """);
    }

    private static void createActivityLogsTable(Statement stmt) throws Exception {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS activity_logs (
                id SERIAL PRIMARY KEY,

                timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                actor_id INTEGER REFERENCES persons(id) ON DELETE SET NULL,
                actor_role VARCHAR(40),

                action_type VARCHAR(60) NOT NULL CHECK (
                    action_type IN (
                        'LOGIN',
                        'LOGOUT',
                        'CREATE_USER',
                        'READ_USER',
                        'UPDATE_PROFILE',
                        'DELETE_USER',
                        'CREATE_CATEGORY',
                        'UPDATE_CATEGORY',
                        'DELETE_CATEGORY',
                        'CREATE_ITEM',
                        'UPDATE_ITEM',
                        'DELETE_ITEM',
                        'MODERATE_ITEM',
                        'CREATE_AUCTION',
                        'UPDATE_AUCTION',
                        'CANCEL_AUCTION',
                        'DELETE_AUCTION',
                        'PROCESS_AUCTION',
                        'PLACE_BID',
                        'REQUEST_TOP_UP',
                        'APPROVE_TOP_UP',
                        'REJECT_TOP_UP',
                        'CREATE_PAYMENT',
                        'UPDATE_PAYMENT_STATUS',
                        'READ_REPORT',
                        'EXPORT_REPORT'
                    )
                ),

                target_entity VARCHAR(80),
                target_id INTEGER,
                description TEXT
            )
        """);
    }

    private static void createIndexes(Statement stmt) throws Exception {
        // persons
        stmt.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_persons_email_lower
            ON persons (LOWER(email))
        """);

        stmt.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_persons_username_lower
            ON persons (LOWER(username))
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_persons_role
            ON persons(role)
        """);

        // categories
        stmt.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_name_lower
            ON categories (LOWER(name))
        """);

        // items
        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_items_seller_id
            ON items(seller_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_items_category_id
            ON items(category_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_items_status
            ON items(status)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_items_condition
            ON items(condition)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_items_starting_price
            ON items(starting_price)
        """);

        // item images
        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_item_images_item_id
            ON item_images(item_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_item_images_item_order
            ON item_images(item_id, display_order)
        """);

        // auctions
        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_auctions_item_id
            ON auctions(item_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_auctions_status
            ON auctions(status)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_auctions_end_date_time
            ON auctions(end_date_time)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_auctions_recommended
            ON auctions(recommended)
        """);

        // bids
        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_bids_auction_id
            ON bids(auction_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_bids_bidder_id
            ON bids(bidder_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_bids_auction_amount
            ON bids(auction_id, amount DESC)
        """);

        // watchlists
        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_auction_watchlists_user_id
            ON auction_watchlists(user_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_auction_watchlists_auction_id
            ON auction_watchlists(auction_id)
        """);

        // payments
        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_payments_auction_id
            ON payments(auction_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_payments_buyer_id
            ON payments(buyer_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_payments_seller_id
            ON payments(seller_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_payments_status
            ON payments(status)
        """);

        // activity logs
        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_activity_logs_actor_id
            ON activity_logs(actor_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_activity_logs_action_type
            ON activity_logs(action_type)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_activity_logs_target
            ON activity_logs(target_entity, target_id)
        """);

        stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_activity_logs_timestamp
            ON activity_logs(timestamp DESC)
        """);
    }
}
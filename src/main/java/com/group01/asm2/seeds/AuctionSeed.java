package com.group01.asm2.seeds;

import com.group01.asm2.enums.AuctionStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AuctionSeed implements Seeder {

    @Override
    public void seed(Connection conn) throws Exception {
        LocalDateTime now = LocalDateTime.now();

        insertAuction(
            conn,
            "Vintage Film Camera",
            AuctionStatus.ACTIVE,
            now.minusDays(1),
            now.plusDays(6),
            false
        );

        insertAuction(
            conn,
            "Gaming Laptop",
            AuctionStatus.ACTIVE,
            now.minusHours(12),
            now.plusDays(5),
            true
        );

        insertAuction(
            conn,
            "Mountain Bike",
            AuctionStatus.ACTIVE,
            now.minusDays(2),
            now.plusDays(3),
            false
        );

        insertAuction(
            conn,
            "Mechanical Keyboard",
            AuctionStatus.SOLD,
            now.minusDays(10),
            now.minusDays(2),
            true
        );

        insertAuction(
            conn,
            "Wooden Coffee Table",
            AuctionStatus.UNSOLD,
            now.minusDays(9),
            now.minusDays(1),
            false
        );
    }

    private void insertAuction(
        Connection conn,
        String itemTitle,
        AuctionStatus status,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        boolean recommended
    ) throws Exception {
        String sql = """
            INSERT INTO auctions (
                item_id,
                status,
                current_highest_bid_id,
                winner_id,
                final_sale_price,
                start_date_time,
                end_date_time,
                recommended
            )
            SELECT i.id, ?, NULL, NULL, NULL, ?, ?, ?
            FROM items i
            WHERE LOWER(i.title) = LOWER(?)
              AND NOT EXISTS (
                  SELECT 1
                  FROM auctions a
                  WHERE a.item_id = i.id
              )
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String normalizedItemTitle = itemTitle.trim();

            ps.setString(1, status.name());
            ps.setTimestamp(2, Timestamp.valueOf(startDateTime));
            ps.setTimestamp(3, Timestamp.valueOf(endDateTime));
            ps.setBoolean(4, recommended);

            ps.setString(5, normalizedItemTitle);

            ps.executeUpdate();
        }
    }
}
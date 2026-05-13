package com.group01.asm2.seeds;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class BidSeed implements Seeder {

    @Override
    public void seed(Connection conn) throws Exception {
        insertBid(conn, "Vintage Film Camera", "buyer1", BigDecimal.valueOf(130));
        insertBid(conn, "Vintage Film Camera", "buyer2", BigDecimal.valueOf(155));

        insertBid(conn, "Gaming Laptop", "buyer1", BigDecimal.valueOf(950));
        insertBid(conn, "Gaming Laptop", "buyer2", BigDecimal.valueOf(1050));

        insertBid(conn, "Mechanical Keyboard", "buyer1", BigDecimal.valueOf(110));

        updateAuctionHighestBid(conn, "Vintage Film Camera");
        updateAuctionHighestBid(conn, "Gaming Laptop");
        updateSoldAuctionWinner(conn, "Mechanical Keyboard");
    }

    private void insertBid(
        Connection conn,
        String itemTitle,
        String bidderUsername,
        BigDecimal amount
    ) throws Exception {
        String sql = """
            INSERT INTO bids (
                auction_id,
                item_id,
                bidder_id,
                amount,
                bid_date_time
            )
            SELECT a.id, i.id, p.id, ?, CURRENT_TIMESTAMP
            FROM auctions a
            JOIN items i ON a.item_id = i.id
            JOIN persons p ON LOWER(p.username) = LOWER(?)
            WHERE LOWER(i.title) = LOWER(?)
              AND NOT EXISTS (
                  SELECT 1
                  FROM bids b
                  WHERE b.auction_id = a.id
                    AND b.item_id = i.id
                    AND b.bidder_id = p.id
                    AND b.amount = ?
              )
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String normalizedItemTitle = itemTitle.trim();
            String normalizedBidderUsername = bidderUsername.trim().toLowerCase();

            ps.setBigDecimal(1, amount);
            ps.setString(2, normalizedBidderUsername);
            ps.setString(3, normalizedItemTitle);
            ps.setBigDecimal(4, amount);

            ps.executeUpdate();
        }
    }

    private void updateAuctionHighestBid(Connection conn, String itemTitle) throws Exception {
        String sql = """
            UPDATE auctions a
            SET current_highest_bid_id = highest_bid.id,
                updated_at = CURRENT_TIMESTAMP
            FROM (
                SELECT b.id, b.auction_id
                FROM bids b
                JOIN auctions inner_a ON b.auction_id = inner_a.id
                JOIN items i ON inner_a.item_id = i.id
                WHERE LOWER(i.title) = LOWER(?)
                ORDER BY b.amount DESC, b.bid_date_time ASC
                LIMIT 1
            ) AS highest_bid
            WHERE a.id = highest_bid.auction_id
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemTitle.trim());
            ps.executeUpdate();
        }
    }

    private void updateSoldAuctionWinner(Connection conn, String itemTitle) throws Exception {
        String sql = """
            UPDATE auctions a
            SET current_highest_bid_id = highest_bid.id,
                winner_id = highest_bid.bidder_id,
                final_sale_price = highest_bid.amount,
                status = 'SOLD',
                updated_at = CURRENT_TIMESTAMP
            FROM (
                SELECT b.id, b.bidder_id, b.amount, b.auction_id
                FROM bids b
                JOIN auctions inner_a ON b.auction_id = inner_a.id
                JOIN items i ON inner_a.item_id = i.id
                WHERE LOWER(i.title) = LOWER(?)
                ORDER BY b.amount DESC, b.bid_date_time ASC
                LIMIT 1
            ) AS highest_bid
            WHERE a.id = highest_bid.auction_id
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemTitle.trim());
            ps.executeUpdate();
        }
    }
}
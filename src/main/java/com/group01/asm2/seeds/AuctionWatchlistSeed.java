package com.group01.asm2.seeds;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AuctionWatchlistSeed implements Seeder {

    @Override
    public void seed(Connection conn) throws Exception {
        insertWatchlist(conn, "buyer1", "Vintage Film Camera");
        insertWatchlist(conn, "buyer1", "Gaming Laptop");
        insertWatchlist(conn, "buyer2", "Gaming Laptop");
        insertWatchlist(conn, "buyer2", "Mountain Bike");
        insertWatchlist(conn, "seller1", "Vintage Film Camera");
    }

    private void insertWatchlist(
        Connection conn,
        String username,
        String itemTitle
    ) throws Exception {
        String sql = """
            INSERT INTO auction_watchlists (
                user_id,
                auction_id
            )
            SELECT p.id, a.id
            FROM persons p
            JOIN auctions a ON TRUE
            JOIN items i ON a.item_id = i.id
            WHERE LOWER(p.username) = LOWER(?)
              AND LOWER(i.title) = LOWER(?)
              AND NOT EXISTS (
                  SELECT 1
                  FROM auction_watchlists aw
                  WHERE aw.user_id = p.id
                    AND aw.auction_id = a.id
              )
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            ps.setString(2, itemTitle.trim());

            ps.executeUpdate();
        }
    }
}
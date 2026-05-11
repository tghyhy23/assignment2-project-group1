package com.group01.asm2.repositories;

import com.group01.asm2.db.SqlExecutor;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Auction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionRepository {
    public Auction createAuction(Auction auction) {
        String sql = getCreateAuctionSql();

        return SqlExecutor.queryOne(
            sql,
            ps -> bindCreateAuction(ps, auction),
            this::mapAuction
        ).orElseThrow(() -> AppException.database("Failed to create auction."));
    }

    public Auction createAuction(Connection conn, Auction auction) {
        String sql = getCreateAuctionSql();

        return SqlExecutor.queryOne(
            conn,
            sql,
            ps -> bindCreateAuction(ps, auction),
            this::mapAuction
        ).orElseThrow(() -> AppException.database("Failed to create auction."));
    }

    public Auction readAuctionById(Integer id) {
        String sql = """
            SELECT id, item_id, status, current_highest_bid_id, winner_id,
                   final_sale_price, start_date_time, end_date_time,
                   created_at, updated_at, recommended
            FROM auctions
            WHERE id = ?
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, id),
            this::mapAuction
        ).orElse(null);
    }

    public Auction readAuctionByItemId(Integer itemId) {
        String sql = """
            SELECT id, item_id, status, current_highest_bid_id, winner_id,
                   final_sale_price, start_date_time, end_date_time,
                   created_at, updated_at, recommended
            FROM auctions
            WHERE item_id = ?
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, itemId),
            this::mapAuction
        ).orElse(null);
    }

    public List<Auction> readAuctions() {
        String sql = """
            SELECT id, item_id, status, current_highest_bid_id, winner_id,
                   final_sale_price, start_date_time, end_date_time,
                   created_at, updated_at, recommended
            FROM auctions
            ORDER BY created_at DESC, id DESC
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> {
            },
            this::mapAuction
        );
    }

    public List<Auction> readActiveAuctions() {
        String sql = """
            SELECT id, item_id, status, current_highest_bid_id, winner_id,
                   final_sale_price, start_date_time, end_date_time,
                   created_at, updated_at, recommended
            FROM auctions
            WHERE status = 'ACTIVE'
              AND end_date_time > CURRENT_TIMESTAMP
            ORDER BY end_date_time ASC, id DESC
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> {
            },
            this::mapAuction
        );
    }

    public List<Auction> readAuctionsBySellerId(Integer sellerId) {
        String sql = """
            SELECT a.id, a.item_id, a.status, a.current_highest_bid_id, a.winner_id,
                   a.final_sale_price, a.start_date_time, a.end_date_time,
                   a.created_at, a.updated_at, a.recommended
            FROM auctions a
            JOIN items i ON i.id = a.item_id
            WHERE i.seller_id = ?
            ORDER BY a.created_at DESC, a.id DESC
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, sellerId),
            this::mapAuction
        );
    }

    public List<Auction> readDueActiveAuctions(LocalDateTime now) {
        String sql = """
            SELECT id, item_id, status, current_highest_bid_id, winner_id,
                   final_sale_price, start_date_time, end_date_time,
                   created_at, updated_at, recommended
            FROM auctions
            WHERE status = 'ACTIVE'
              AND end_date_time <= ?
            ORDER BY end_date_time ASC, id ASC
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setTimestamp(1, Timestamp.valueOf(now)),
            this::mapAuction
        );
    }

    public Auction updateAuction(Auction auction) {
        String sql = getUpdateAuctionSql();

        return SqlExecutor.queryOne(
            sql,
            ps -> bindUpdateAuction(ps, auction),
            this::mapAuction
        ).orElse(null);
    }

    public Auction updateAuction(Connection conn, Auction auction) {
        String sql = getUpdateAuctionSql();

        return SqlExecutor.queryOne(
            conn,
            sql,
            ps -> bindUpdateAuction(ps, auction),
            this::mapAuction
        ).orElse(null);
    }

    public void deleteAuction(Integer id) {
        String sql = """
            DELETE FROM auctions
            WHERE id = ?
        """;

        SqlExecutor.update(
            sql,
            ps -> ps.setInt(1, id)
        );
    }

    public void deleteAuction(Connection conn, Integer id) {
        String sql = """
            DELETE FROM auctions
            WHERE id = ?
        """;

        SqlExecutor.update(
            conn,
            sql,
            ps -> ps.setInt(1, id)
        );
    }

    public boolean existsAuctionForItem(Integer itemId) {
        String sql = """
            SELECT 1
            FROM auctions
            WHERE item_id = ?
            LIMIT 1
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, itemId),
            rs -> true
        ).orElse(false);
    }

    public boolean hasBids(Integer auctionId) {
        String sql = """
            SELECT 1
            FROM bids
            WHERE auction_id = ?
            LIMIT 1
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, auctionId),
            rs -> true
        ).orElse(false);
    }

    public boolean hasPayment(Integer auctionId) {
        String sql = """
            SELECT 1
            FROM payments
            WHERE auction_id = ?
            LIMIT 1
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, auctionId),
            rs -> true
        ).orElse(false);
    }

    private String getCreateAuctionSql() {
        return """
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
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, item_id, status, current_highest_bid_id, winner_id,
                      final_sale_price, start_date_time, end_date_time,
                      created_at, updated_at, recommended
        """;
    }

    private String getUpdateAuctionSql() {
        return """
            UPDATE auctions
            SET status = ?,
                current_highest_bid_id = ?,
                winner_id = ?,
                final_sale_price = ?,
                start_date_time = ?,
                end_date_time = ?,
                recommended = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING id, item_id, status, current_highest_bid_id, winner_id,
                      final_sale_price, start_date_time, end_date_time,
                      created_at, updated_at, recommended
        """;
    }

    private void bindCreateAuction(java.sql.PreparedStatement ps, Auction auction) throws Exception {
        ps.setInt(1, auction.getItemId());
        ps.setString(2, auction.getStatus().name());

        if (auction.getCurrentHighestBidId() == null) {
            ps.setNull(3, Types.INTEGER);
        } else {
            ps.setInt(3, auction.getCurrentHighestBidId());
        }

        if (auction.getWinnerId() == null) {
            ps.setNull(4, Types.INTEGER);
        } else {
            ps.setInt(4, auction.getWinnerId());
        }

        if (auction.getFinalSalePrice() == null) {
            ps.setNull(5, Types.NUMERIC);
        } else {
            ps.setBigDecimal(5, auction.getFinalSalePrice());
        }

        ps.setTimestamp(6, Timestamp.valueOf(auction.getStartDateTime()));
        ps.setTimestamp(7, Timestamp.valueOf(auction.getEndDateTime()));
        ps.setBoolean(8, auction.isRecommended());
    }

    private void bindUpdateAuction(java.sql.PreparedStatement ps, Auction auction) throws Exception {
        ps.setString(1, auction.getStatus().name());

        if (auction.getCurrentHighestBidId() == null) {
            ps.setNull(2, Types.INTEGER);
        } else {
            ps.setInt(2, auction.getCurrentHighestBidId());
        }

        if (auction.getWinnerId() == null) {
            ps.setNull(3, Types.INTEGER);
        } else {
            ps.setInt(3, auction.getWinnerId());
        }

        if (auction.getFinalSalePrice() == null) {
            ps.setNull(4, Types.NUMERIC);
        } else {
            ps.setBigDecimal(4, auction.getFinalSalePrice());
        }

        ps.setTimestamp(5, Timestamp.valueOf(auction.getStartDateTime()));
        ps.setTimestamp(6, Timestamp.valueOf(auction.getEndDateTime()));
        ps.setBoolean(7, auction.isRecommended());
        ps.setInt(8, auction.getId());
    }

    private Auction mapAuction(ResultSet rs) throws Exception {
        Auction auction = new Auction();

        auction.setId(rs.getInt("id"));
        auction.setItemId(rs.getInt("item_id"));

        String status = rs.getString("status");
        if (status != null) {
            auction.setStatus(AuctionStatus.valueOf(status));
        }

        int currentHighestBidId = rs.getInt("current_highest_bid_id");
        auction.setCurrentHighestBidId(rs.wasNull() ? null : currentHighestBidId);

        int winnerId = rs.getInt("winner_id");
        auction.setWinnerId(rs.wasNull() ? null : winnerId);

        auction.setFinalSalePrice(rs.getBigDecimal("final_sale_price"));

        Timestamp startDateTime = rs.getTimestamp("start_date_time");
        if (startDateTime != null) {
            auction.setStartDateTime(startDateTime.toLocalDateTime());
        }

        Timestamp endDateTime = rs.getTimestamp("end_date_time");
        if (endDateTime != null) {
            auction.setEndDateTime(endDateTime.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            auction.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            auction.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        auction.setRecommended(rs.getBoolean("recommended"));

        return auction;
    }
}
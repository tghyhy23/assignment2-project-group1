package com.group01.asm2.repositories;

/**
 * @author Group 01
 */

import com.group01.asm2.db.SqlExecutor;
import com.group01.asm2.dtos.AuctionCardDto;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.models.Auction;
import com.group01.asm2.models.AuctionWatchlist;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionWatchlistRepository {

    public AuctionWatchlist createAuctionWatchlist(AuctionWatchlist watchlist) {
        String sql = """
            INSERT INTO auction_watchlists (
                user_id,
                auction_id
            )
            VALUES (?, ?)
            RETURNING *
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setInt(1, watchlist.getUserId());
                ps.setInt(2, watchlist.getAuctionId());
            },
            this::mapWatchlistRow
        ).orElse(null);
    }

    public AuctionWatchlist readAuctionWatchlistById(Integer watchlistId) {
        String sql = """
            SELECT *
            FROM auction_watchlists
            WHERE id = ?
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, watchlistId),
            this::mapWatchlistRow
        ).orElse(null);
    }

    public AuctionWatchlist readAuctionWatchlistByUserIdAndAuctionId(Integer userId, Integer auctionId) {
        String sql = """
            SELECT *
            FROM auction_watchlists
            WHERE user_id = ?
              AND auction_id = ?
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setInt(1, userId);
                ps.setInt(2, auctionId);
            },
            this::mapWatchlistRow
        ).orElse(null);
    }

    public List<AuctionWatchlist> readAuctionWatchlistsByUserId(Integer userId) {
        String sql = """
            SELECT *
            FROM auction_watchlists
            WHERE user_id = ?
            ORDER BY created_at DESC
            """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, userId),
            this::mapWatchlistRow
        );
    }

    public List<AuctionCardDto> readWatchlistAuctionCardsByUserId(Integer userId) {
        String sql = """
            SELECT
                a.id AS auction_id,
                a.item_id AS auction_item_id,
                a.status AS auction_status,
                a.current_highest_bid_id AS auction_current_highest_bid_id,
                a.winner_id AS auction_winner_id,
                a.final_sale_price AS auction_final_sale_price,
                a.start_date_time AS auction_start_date_time,
                a.end_date_time AS auction_end_date_time,
                a.created_at AS auction_created_at,
                a.updated_at AS auction_updated_at,
                a.recommended AS auction_recommended,

                i.id AS item_id,
                i.seller_id AS item_seller_id,
                i.category_id AS item_category_id,
                i.title AS item_title,
                i.condition AS item_condition,
                i.starting_price AS item_starting_price,

                c.name AS category_name,

                p.username AS seller_username,

                img.image_url AS primary_image_url,

                COALESCE(highest_bid.amount, i.starting_price) AS current_bid_amount,

                (
                    SELECT COUNT(*)
                    FROM bids bid_count
                    WHERE bid_count.auction_id = a.id
                ) AS bid_count,

                TRUE AS watching

            FROM auction_watchlists aw
            JOIN auctions a
                ON aw.auction_id = a.id
            JOIN items i
                ON a.item_id = i.id
            JOIN categories c
                ON i.category_id = c.id
            JOIN persons p
                ON i.seller_id = p.id
            LEFT JOIN item_images img
                ON img.item_id = i.id
               AND img.display_order = 0
            LEFT JOIN bids highest_bid
                ON highest_bid.id = a.current_highest_bid_id
            WHERE aw.user_id = ?
            ORDER BY aw.created_at DESC
            """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, userId),
            this::mapAuctionCardRow
        );
    }

    public List<Auction> readWatchedAuctionsByUserId(Integer userId) {
        String sql = """
            SELECT
                a.id AS auction_id,
                a.item_id AS auction_item_id,
                a.status AS auction_status,
                a.current_highest_bid_id AS auction_current_highest_bid_id,
                a.winner_id AS auction_winner_id,
                a.final_sale_price AS auction_final_sale_price,
                a.start_date_time AS auction_start_date_time,
                a.end_date_time AS auction_end_date_time,
                a.created_at AS auction_created_at,
                a.updated_at AS auction_updated_at,
                a.recommended AS auction_recommended
            FROM auction_watchlists aw
            JOIN auctions a
                ON aw.auction_id = a.id
            WHERE aw.user_id = ?
            ORDER BY aw.created_at DESC
            """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, userId),
            this::mapAuctionRow
        );
    }

    public boolean existsByUserIdAndAuctionId(Integer userId, Integer auctionId) {
        String sql = """
            SELECT 1
            FROM auction_watchlists
            WHERE user_id = ?
              AND auction_id = ?
            LIMIT 1
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setInt(1, userId);
                ps.setInt(2, auctionId);
            },
            rs -> true
        ).orElse(false);
    }

    public boolean existsAuctionById(Integer auctionId) {
        String sql = """
            SELECT 1
            FROM auctions
            WHERE id = ?
            LIMIT 1
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, auctionId),
            rs -> true
        ).orElse(false);
    }

    public boolean existsActiveAuctionById(Integer auctionId) {
        String sql = """
            SELECT 1
            FROM auctions
            WHERE id = ?
              AND status = 'ACTIVE'
            LIMIT 1
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, auctionId),
            rs -> true
        ).orElse(false);
    }

    public void deleteAuctionWatchlistByUserIdAndAuctionId(Integer userId, Integer auctionId) {
        String sql = """
            DELETE FROM auction_watchlists
            WHERE user_id = ?
              AND auction_id = ?
            """;

        SqlExecutor.update(
            sql,
            ps -> {
                ps.setInt(1, userId);
                ps.setInt(2, auctionId);
            }
        );
    }

    public void deleteAuctionWatchlistsByUserId(Integer userId) {
        String sql = """
            DELETE FROM auction_watchlists
            WHERE user_id = ?
            """;

        SqlExecutor.update(
            sql,
            ps -> ps.setInt(1, userId)
        );
    }

    public void deleteAuctionWatchlistsByAuctionId(Integer auctionId) {
        String sql = """
            DELETE FROM auction_watchlists
            WHERE auction_id = ?
            """;

        SqlExecutor.update(
            sql,
            ps -> ps.setInt(1, auctionId)
        );
    }

    private AuctionWatchlist mapWatchlistRow(ResultSet rs) throws Exception {
        return new AuctionWatchlist(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("auction_id"),
            toLocalDateTime(rs.getTimestamp("created_at"))
        );
    }

    private AuctionCardDto mapAuctionCardRow(ResultSet rs) throws Exception {
        AuctionStatus status = AuctionStatus.valueOf(rs.getString("auction_status"));

        Auction auction = new Auction(
            rs.getInt("auction_id"),
            rs.getInt("auction_item_id"),
            status,
            getNullableInteger(rs, "auction_current_highest_bid_id"),
            getNullableInteger(rs, "auction_winner_id"),
            rs.getBigDecimal("auction_final_sale_price"),
            toLocalDateTime(rs.getTimestamp("auction_start_date_time")),
            toLocalDateTime(rs.getTimestamp("auction_end_date_time")),
            toLocalDateTime(rs.getTimestamp("auction_created_at")),
            toLocalDateTime(rs.getTimestamp("auction_updated_at")),
            rs.getBoolean("auction_recommended")
        );

        return new AuctionCardDto(
            auction,
            rs.getInt("auction_id"),
            rs.getInt("item_id"),
            rs.getInt("item_seller_id"),
            rs.getInt("item_category_id"),
            rs.getString("item_title"),
            rs.getString("item_condition"),
            rs.getString("category_name"),
            rs.getString("seller_username"),
            rs.getString("primary_image_url"),
            status,
            rs.getBigDecimal("item_starting_price"),
            rs.getBigDecimal("current_bid_amount"),
            toLocalDateTime(rs.getTimestamp("auction_start_date_time")),
            toLocalDateTime(rs.getTimestamp("auction_end_date_time")),
            rs.getInt("bid_count"),
            rs.getBoolean("auction_recommended"),
            rs.getBoolean("watching")
        );
    }

    private Auction mapAuctionRow(ResultSet rs) throws Exception {
        return new Auction(
            rs.getInt("auction_id"),
            rs.getInt("auction_item_id"),
            AuctionStatus.valueOf(rs.getString("auction_status")),
            getNullableInteger(rs, "auction_current_highest_bid_id"),
            getNullableInteger(rs, "auction_winner_id"),
            rs.getBigDecimal("auction_final_sale_price"),
            toLocalDateTime(rs.getTimestamp("auction_start_date_time")),
            toLocalDateTime(rs.getTimestamp("auction_end_date_time")),
            toLocalDateTime(rs.getTimestamp("auction_created_at")),
            toLocalDateTime(rs.getTimestamp("auction_updated_at")),
            rs.getBoolean("auction_recommended")
        );
    }

    private Integer getNullableInteger(ResultSet rs, String columnName) throws Exception {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
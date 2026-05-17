package com.group01.asm2.repositories;

/**
 * @author Group 01
 */

import com.group01.asm2.db.SqlExecutor;
import com.group01.asm2.dtos.BidHistoryDto;
import com.group01.asm2.dtos.reports.BuyerBiddingHistoryReportDto;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.enums.ItemCondition;
import com.group01.asm2.enums.ItemStatus;
import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Bid;
import com.group01.asm2.models.Item;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class BidRepository {

    public Bid createBid(Bid bid) {
        String sql = """
            INSERT INTO bids (
                auction_id,
                item_id,
                bidder_id,
                amount,
                bid_date_time
            )
            VALUES (?, ?, ?, ?, ?)
            RETURNING *
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setInt(1, bid.getAuctionId());
                ps.setInt(2, bid.getItemId());
                ps.setInt(3, bid.getBidderId());
                ps.setBigDecimal(4, bid.getAmount());
                ps.setTimestamp(5, Timestamp.valueOf(bid.getBidDateTime()));
            },
            this::mapBidRow
        ).orElse(null);
    }

    public Bid createBid(Connection conn, Bid bid) {
        String sql = """
            INSERT INTO bids (
                auction_id,
                item_id,
                bidder_id,
                amount,
                bid_date_time
            )
            VALUES (?, ?, ?, ?, ?)
            RETURNING *
            """;

        return SqlExecutor.queryOne(
            conn,
            sql,
            ps -> {
                ps.setInt(1, bid.getAuctionId());
                ps.setInt(2, bid.getItemId());
                ps.setInt(3, bid.getBidderId());
                ps.setBigDecimal(4, bid.getAmount());
                ps.setTimestamp(5, Timestamp.valueOf(bid.getBidDateTime()));
            },
            this::mapBidRow
        ).orElse(null);
    }

    public Bid readBidById(Integer bidId) {
        String sql = """
            SELECT *
            FROM bids
            WHERE id = ?
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, bidId),
            this::mapBidRow
        ).orElse(null);
    }

    public Bid readBidById(Connection conn, Integer bidId) {
        String sql = """
            SELECT *
            FROM bids
            WHERE id = ?
            """;

        return SqlExecutor.queryOne(
            conn,
            sql,
            ps -> ps.setInt(1, bidId),
            this::mapBidRow
        ).orElse(null);
    }

    public List<Bid> readBidsByAuctionId(Integer auctionId) {
        String sql = """
            SELECT *
            FROM bids
            WHERE auction_id = ?
            ORDER BY amount DESC, bid_date_time ASC
            """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, auctionId),
            this::mapBidRow
        );
    }

    public List<Bid> readBidsByBidderId(Integer bidderId) {
        String sql = """
            SELECT *
            FROM bids
            WHERE bidder_id = ?
            ORDER BY bid_date_time DESC
            """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, bidderId),
            this::mapBidRow
        );
    }

    public Bid readHighestBidByAuctionId(Integer auctionId) {
        String sql = """
            SELECT *
            FROM bids
            WHERE auction_id = ?
            ORDER BY amount DESC, bid_date_time ASC
            LIMIT 1
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, auctionId),
            this::mapBidRow
        ).orElse(null);
    }

    public Bid readHighestBidByAuctionId(Connection conn, Integer auctionId) {
        String sql = """
            SELECT *
            FROM bids
            WHERE auction_id = ?
            ORDER BY amount DESC, bid_date_time ASC
            LIMIT 1
            """;

        return SqlExecutor.queryOne(
            conn,
            sql,
            ps -> ps.setInt(1, auctionId),
            this::mapBidRow
        ).orElse(null);
    }

    public int countBidsByAuctionId(Integer auctionId) {
        String sql = """
            SELECT COUNT(*) AS bid_count
            FROM bids
            WHERE auction_id = ?
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, auctionId),
            rs -> rs.getInt("bid_count")
        ).orElse(0);
    }

    public void deleteBidById(Integer bidId) {
        String sql = """
            DELETE FROM bids
            WHERE id = ?
            """;

        SqlExecutor.update(
            sql,
            ps -> ps.setInt(1, bidId)
        );
    }

    public Auction readAuctionByIdForUpdate(Connection conn, Integer auctionId) {
        String sql = """
            SELECT
                id,
                item_id,
                status,
                current_highest_bid_id,
                winner_id,
                final_sale_price,
                start_date_time,
                end_date_time,
                created_at,
                updated_at,
                recommended
            FROM auctions
            WHERE id = ?
            FOR UPDATE
            """;

        return SqlExecutor.queryOne(
            conn,
            sql,
            ps -> ps.setInt(1, auctionId),
            this::mapAuctionRow
        ).orElse(null);
    }

    public Item readItemById(Connection conn, Integer itemId) {
        String sql = """
            SELECT
                id,
                title,
                description,
                category_id,
                seller_id,
                starting_price,
                reserve_price,
                condition,
                status,
                created_at,
                updated_at
            FROM items
            WHERE id = ?
            """;

        return SqlExecutor.queryOne(
            conn,
            sql,
            ps -> ps.setInt(1, itemId),
            this::mapItemRow
        ).orElse(null);
    }

    public void updateAuctionCurrentHighestBidId(Connection conn, Integer auctionId, Integer bidId) {
        String sql = """
            UPDATE auctions
            SET current_highest_bid_id = ?,
                updated_at = NOW()
            WHERE id = ?
            """;

        SqlExecutor.update(
            conn,
            sql,
            ps -> {
                ps.setInt(1, bidId);
                ps.setInt(2, auctionId);
            }
        );
    }

    public List<BidHistoryDto> readBidHistoryByBidderId(Integer bidderId) {
        String sql = """
            SELECT
                b.id AS bid_id,
                b.auction_id AS bid_auction_id,
                b.item_id AS bid_item_id,
                b.bidder_id AS bid_bidder_id,
                b.amount AS bid_amount,
                b.bid_date_time AS bid_date_time,

                i.title AS item_name,

                a.status AS auction_status,
                a.current_highest_bid_id AS current_highest_bid_id,
                a.winner_id AS winner_id,

                COALESCE(highest_bid.amount, i.starting_price) AS current_highest_bid_amount,

                pay.status AS payment_status

            FROM bids b
            JOIN auctions a
                ON b.auction_id = a.id
            JOIN items i
                ON b.item_id = i.id
            LEFT JOIN bids highest_bid
                ON highest_bid.id = a.current_highest_bid_id
            LEFT JOIN payments pay
                ON pay.auction_id = a.id
               AND pay.buyer_id = b.bidder_id
            WHERE b.bidder_id = ?
            ORDER BY b.bid_date_time DESC
            """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, bidderId),
            this::mapBidHistoryRow
        );
    }

    public List<BuyerBiddingHistoryReportDto> readBuyerBiddingHistoryReport(Integer buyerId) {
        String sql = """
        SELECT
            b.id AS bid_id,
            a.id AS auction_id,
            i.title AS item_title,
            c.name AS category_name,
            b.amount AS bid_amount,
            b.bid_date_time AS bid_date_time,
            a.status AS auction_status,
            COALESCE(highest_bid.amount, i.starting_price) AS current_highest_bid,
            CASE
                WHEN a.status = 'CANCELLED' THEN 'CANCELLED'
                WHEN a.status = 'ACTIVE'
                     AND a.current_highest_bid_id = b.id THEN 'CURRENTLY_WINNING'
                WHEN a.status = 'ACTIVE' THEN 'OUTBID'
                WHEN a.status = 'SOLD'
                     AND a.current_highest_bid_id = b.id
                     AND a.winner_id = b.bidder_id THEN 'WON'
                WHEN a.status IN ('ENDED', 'UNSOLD', 'SOLD') THEN 'LOST'
                ELSE 'BID_PLACED'
            END AS bid_result
        FROM bids b
        JOIN auctions a
            ON b.auction_id = a.id
        JOIN items i
            ON b.item_id = i.id
        LEFT JOIN categories c
            ON i.category_id = c.id
        LEFT JOIN bids highest_bid
            ON highest_bid.id = a.current_highest_bid_id
        WHERE b.bidder_id = ?
        ORDER BY b.bid_date_time DESC, b.id DESC
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, buyerId),
            rs -> new BuyerBiddingHistoryReportDto(
                rs.getInt("bid_id"),
                rs.getInt("auction_id"),
                rs.getString("item_title"),
                rs.getString("category_name"),
                rs.getBigDecimal("bid_amount"),
                toLocalDateTime(rs.getTimestamp("bid_date_time")),
                rs.getString("auction_status"),
                rs.getBigDecimal("current_highest_bid"),
                rs.getString("bid_result")
            )
        );
    }

    private BidHistoryDto mapBidHistoryRow(ResultSet rs) throws Exception {
        Bid bid = new Bid(
            rs.getInt("bid_id"),
            rs.getInt("bid_auction_id"),
            rs.getInt("bid_item_id"),
            rs.getInt("bid_bidder_id"),
            rs.getBigDecimal("bid_amount"),
            toLocalDateTime(rs.getTimestamp("bid_date_time"))
        );

        String itemName = rs.getString("item_name");
        String auctionStatus = rs.getString("auction_status");
        Integer currentHighestBidId = getNullableInteger(rs, "current_highest_bid_id");
        Integer winnerId = getNullableInteger(rs, "winner_id");
        BigDecimal currentHighestBidAmount = rs.getBigDecimal("current_highest_bid_amount");
        String rawPaymentStatus = rs.getString("payment_status");

        String bidStatus = calculateBidStatus(
            bid,
            auctionStatus,
            currentHighestBidId,
            winnerId
        );

        String paymentStatus = calculatePaymentStatus(
            bidStatus,
            auctionStatus,
            rawPaymentStatus
        );

        String finalResultText = createFinalResultText(
            bidStatus,
            paymentStatus
        );

        return new BidHistoryDto(
            bid,
            itemName,
            null,
            bidStatus,
            paymentStatus,
            bid.getAmount(),
            currentHighestBidAmount,
            bid.getBidDateTime(),
            finalResultText
        );
    }

    private String calculateBidStatus(
        Bid bid,
        String auctionStatus,
        Integer currentHighestBidId,
        Integer winnerId
    ) {
        if ("CANCELLED".equalsIgnoreCase(auctionStatus)) {
            return "Cancelled";
        }

        boolean isCurrentHighestBid = currentHighestBidId != null
            && currentHighestBidId.equals(bid.getId());

        if ("ACTIVE".equalsIgnoreCase(auctionStatus)) {
            return isCurrentHighestBid ? "Winning" : "Outbid";
        }

        boolean isWinningFinalBid = isCurrentHighestBid
            && winnerId != null
            && winnerId.equals(bid.getBidderId());

        if (isWinningFinalBid) {
            return "Won";
        }

        return "Lost";
    }

    private String calculatePaymentStatus(
        String bidStatus,
        String auctionStatus,
        String rawPaymentStatus
    ) {
        if ("Cancelled".equalsIgnoreCase(bidStatus)
            || "CANCELLED".equalsIgnoreCase(auctionStatus)) {
            return "Cancelled";
        }

        if (!"Won".equalsIgnoreCase(bidStatus)) {
            return "Not Required";
        }

        if (rawPaymentStatus == null || rawPaymentStatus.isBlank()) {
            return "Pending Payment";
        }

        if ("COMPLETED".equalsIgnoreCase(rawPaymentStatus)
            || "PAID".equalsIgnoreCase(rawPaymentStatus)) {
            return "Paid";
        }

        if ("CANCELLED".equalsIgnoreCase(rawPaymentStatus)) {
            return "Cancelled";
        }

        return "Pending Payment";
    }

    private String createFinalResultText(String bidStatus, String paymentStatus) {
        if ("Winning".equalsIgnoreCase(bidStatus)) {
            return "You are currently the highest bidder.";
        }

        if ("Outbid".equalsIgnoreCase(bidStatus)) {
            return "Another bidder has placed a higher bid.";
        }

        if ("Won".equalsIgnoreCase(bidStatus)
            && "Pending Payment".equalsIgnoreCase(paymentStatus)) {
            return "You won this auction. Payment is required.";
        }

        if ("Won".equalsIgnoreCase(bidStatus)
            && "Paid".equalsIgnoreCase(paymentStatus)) {
            return "You won this auction and payment has been completed.";
        }

        if ("Lost".equalsIgnoreCase(bidStatus)) {
            return "The auction ended with another winner.";
        }

        if ("Cancelled".equalsIgnoreCase(bidStatus)) {
            return "This bid or auction was cancelled.";
        }

        return "Bid history record.";
    }

    private Bid mapBidRow(ResultSet rs) throws Exception {
        return new Bid(
            rs.getInt("id"),
            rs.getInt("auction_id"),
            rs.getInt("item_id"),
            rs.getInt("bidder_id"),
            rs.getBigDecimal("amount"),
            toLocalDateTime(rs.getTimestamp("bid_date_time"))
        );
    }

    private Auction mapAuctionRow(ResultSet rs) throws Exception {
        return new Auction(
            rs.getInt("id"),
            rs.getInt("item_id"),
            AuctionStatus.valueOf(rs.getString("status")),
            getNullableInteger(rs, "current_highest_bid_id"),
            getNullableInteger(rs, "winner_id"),
            rs.getBigDecimal("final_sale_price"),
            toLocalDateTime(rs.getTimestamp("start_date_time")),
            toLocalDateTime(rs.getTimestamp("end_date_time")),
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at")),
            rs.getBoolean("recommended")
        );
    }

    private Item mapItemRow(ResultSet rs) throws Exception {
        return new Item(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getInt("category_id"),
            rs.getInt("seller_id"),
            rs.getBigDecimal("starting_price"),
            rs.getBigDecimal("reserve_price"),
            ItemCondition.valueOf(rs.getString("condition")),
            ItemStatus.valueOf(rs.getString("status")),
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
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
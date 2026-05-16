package com.group01.asm2.repositories;

import com.group01.asm2.db.SqlExecutor;
import com.group01.asm2.dtos.AuctionDetailDto;
import com.group01.asm2.dtos.WonAuctionDto;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.enums.ItemCondition;
import com.group01.asm2.enums.ItemStatus;
import com.group01.asm2.enums.PaymentStatus;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.*;

import java.math.BigDecimal;
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

    public AuctionDetailDto readAuction(Integer auctionId) {
        AuctionDetailDto detail = readAuctionDetailBaseById(auctionId);

        if (detail == null || detail.getAuction() == null || detail.getItem() == null) {
            return null;
        }

        Integer itemId = detail.getItem().getId();

        detail.setImages(readItemImagesByItemId(itemId));
        detail.setRecentBids(readRecentBidsByAuctionId(auctionId, 10));
        detail.setPayment(readPaymentByAuctionId(auctionId));

        return detail;
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

    public List<WonAuctionDto> readWonAuctionsByBuyerId(Integer buyerId) {
        String sql = """
        SELECT
            a.id AS auction_id,
            a.item_id AS item_id,
            i.seller_id AS seller_id,
            a.winner_id AS buyer_id,
            p.id AS payment_id,

            i.title AS item_title,
            seller.username AS seller_username,
            c.name AS category_name,
            primary_image.image_url AS primary_image_url,

            a.status AS auction_status,
            p.status AS payment_status,

            a.final_sale_price AS final_sale_price,
            p.total_amount AS total_amount,
            p.commission_amount AS commission_amount,
            p.seller_payout AS seller_payout,

            a.end_date_time AS won_date_time,
            p.payment_date_time AS payment_date_time

        FROM auctions a
        JOIN items i ON i.id = a.item_id
        JOIN persons seller ON seller.id = i.seller_id
        LEFT JOIN categories c ON c.id = i.category_id
        LEFT JOIN payments p ON p.auction_id = a.id
                            AND p.buyer_id = a.winner_id
        LEFT JOIN LATERAL (
            SELECT image_url
            FROM item_images
            WHERE item_id = i.id
            ORDER BY display_order ASC, id ASC
            LIMIT 1
        ) primary_image ON TRUE

        WHERE a.winner_id = ?
          AND a.status = 'SOLD'

        ORDER BY a.end_date_time DESC, a.id DESC
    """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, buyerId),
            this::mapWonAuctionDto
        );
    }

    private AuctionDetailDto readAuctionDetailBaseById(Integer auctionId) {
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
            i.description AS item_description,
            i.condition AS item_condition,
            i.starting_price AS item_starting_price,
            i.reserve_price AS item_reserve_price,
            i.status AS item_status,
            i.created_at AS item_created_at,
            i.updated_at AS item_updated_at,

            c.id AS category_id,
            c.name AS category_name,

            seller.id AS seller_id,
            seller.username AS seller_username,
            seller.email AS seller_email,

            current_bid.id AS current_bid_id,
            current_bid.amount AS current_bid_amount,
            current_bid.bidder_id AS current_bidder_id,
            current_bidder.username AS current_bidder_username,

            COALESCE(bid_summary.bid_count, 0) AS bid_count

        FROM auctions a
        JOIN items i ON i.id = a.item_id
        LEFT JOIN categories c ON c.id = i.category_id
        LEFT JOIN persons seller ON seller.id = i.seller_id
        LEFT JOIN bids current_bid ON current_bid.id = a.current_highest_bid_id
        LEFT JOIN persons current_bidder ON current_bidder.id = current_bid.bidder_id
        LEFT JOIN (
            SELECT auction_id, COUNT(*) AS bid_count
            FROM bids
            GROUP BY auction_id
        ) bid_summary ON bid_summary.auction_id = a.id

        WHERE a.id = ?
    """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, auctionId),
            this::mapAuctionDetailDto
        ).orElse(null);
    }

    private List<ItemImage> readItemImagesByItemId(Integer itemId) {
        String sql = """
        SELECT id, item_id, image_url, display_order
        FROM item_images
        WHERE item_id = ?
        ORDER BY display_order ASC, id ASC
    """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, itemId),
            this::mapItemImage
        );
    }

    private List<Bid> readRecentBidsByAuctionId(Integer auctionId, int limit) {
        String sql = """
        SELECT id, auction_id, item_id, bidder_id, amount, bid_date_time
        FROM bids
        WHERE auction_id = ?
        ORDER BY amount DESC, bid_date_time DESC, id DESC
        LIMIT ?
    """;

        return SqlExecutor.queryMany(
            sql,
            ps -> {
                ps.setInt(1, auctionId);
                ps.setInt(2, limit);
            },
            this::mapBid
        );
    }

    private Payment readPaymentByAuctionId(Integer auctionId) {
        String sql = """
        SELECT id, auction_id, buyer_id, seller_id, status,
               total_amount, commission_amount, seller_payout,
               payment_date_time, created_at, updated_at
        FROM payments
        WHERE auction_id = ?
        ORDER BY payment_date_time DESC NULLS LAST, id DESC
        LIMIT 1
    """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, auctionId),
            this::mapPayment
        ).orElse(null);
    }

    private AuctionDetailDto mapAuctionDetailDto(ResultSet rs) throws Exception {
        Auction auction = new Auction();

        auction.setId(rs.getInt("auction_id"));
        auction.setItemId(rs.getInt("auction_item_id"));

        String auctionStatus = rs.getString("auction_status");
        if (auctionStatus != null) {
            auction.setStatus(AuctionStatus.valueOf(auctionStatus));
        }

        auction.setCurrentHighestBidId(getNullableInt(rs, "auction_current_highest_bid_id"));
        auction.setWinnerId(getNullableInt(rs, "auction_winner_id"));
        auction.setFinalSalePrice(rs.getBigDecimal("auction_final_sale_price"));
        auction.setStartDateTime(getNullableLocalDateTime(rs, "auction_start_date_time"));
        auction.setEndDateTime(getNullableLocalDateTime(rs, "auction_end_date_time"));
        auction.setCreatedAt(getNullableLocalDateTime(rs, "auction_created_at"));
        auction.setUpdatedAt(getNullableLocalDateTime(rs, "auction_updated_at"));
        auction.setRecommended(rs.getBoolean("auction_recommended"));

        Item item = new Item();

        item.setId(rs.getInt("item_id"));
        item.setSellerId(getNullableInt(rs, "item_seller_id"));
        item.setCategoryId(getNullableInt(rs, "item_category_id"));
        item.setTitle(rs.getString("item_title"));
        item.setDescription(rs.getString("item_description"));

        String itemCondition = rs.getString("item_condition");
        if (itemCondition != null) {
            item.setCondition(ItemCondition.valueOf(itemCondition));
        }

        item.setStartingPrice(rs.getBigDecimal("item_starting_price"));
        item.setReservePrice(rs.getBigDecimal("item_reserve_price"));

        String itemStatus = rs.getString("item_status");
        if (itemStatus != null) {
            item.setStatus(ItemStatus.valueOf(itemStatus));
        }

        item.setCreatedAt(getNullableLocalDateTime(rs, "item_created_at"));
        item.setUpdatedAt(getNullableLocalDateTime(rs, "item_updated_at"));

        Category category = null;

        Integer categoryId = getNullableInt(rs, "category_id");
        if (categoryId != null) {
            category = new Category();
            category.setId(categoryId);
            category.setName(rs.getString("category_name"));
        }

        User seller = null;

        Integer sellerId = getNullableInt(rs, "seller_id");
        if (sellerId != null) {
            seller = new User();
            seller.setId(sellerId);
            seller.setUsername(rs.getString("seller_username"));
            seller.setEmail(rs.getString("seller_email"));
        }

        BigDecimal currentBidAmount = rs.getBigDecimal("current_bid_amount");
        Integer currentHighestBidId = getNullableInt(rs, "current_bid_id");
        Integer currentHighestBidderId = getNullableInt(rs, "current_bidder_id");
        String currentHighestBidderUsername = rs.getString("current_bidder_username");

        int bidCount = rs.getInt("bid_count");

        AuctionDetailDto detail = new AuctionDetailDto();

        detail.setAuction(auction);
        detail.setItem(item);
        detail.setCategory(category);
        detail.setSeller(seller);
        detail.setCurrentBidAmount(currentBidAmount);
        detail.setCurrentHighestBidId(currentHighestBidId);
        detail.setCurrentHighestBidderId(currentHighestBidderId);
        detail.setCurrentHighestBidderUsername(currentHighestBidderUsername);
        detail.setBidCount(bidCount);

        return detail;
    }

    private ItemImage mapItemImage(ResultSet rs) throws Exception {
        ItemImage image = new ItemImage();

        image.setId(rs.getInt("id"));
        image.setItemId(rs.getInt("item_id"));
        image.setImageUrl(rs.getString("image_url"));
        image.setDisplayOrder(getNullableInt(rs, "display_order"));

        return image;
    }

    private Bid mapBid(ResultSet rs) throws Exception {
        Bid bid = new Bid();

        bid.setId(rs.getInt("id"));
        bid.setAuctionId(rs.getInt("auction_id"));
        bid.setItemId(getNullableInt(rs, "item_id"));
        bid.setBidderId(rs.getInt("bidder_id"));
        bid.setAmount(rs.getBigDecimal("amount"));
        bid.setBidDateTime(getNullableLocalDateTime(rs, "bid_date_time"));

        return bid;
    }

    private Payment mapPayment(ResultSet rs) throws Exception {
        Payment payment = new Payment();

        payment.setId(rs.getInt("id"));
        payment.setAuctionId(rs.getInt("auction_id"));
        payment.setBuyerId(getNullableInt(rs, "buyer_id"));
        payment.setSellerId(getNullableInt(rs, "seller_id"));

        String paymentStatus = rs.getString("status");
        if (paymentStatus != null) {
            payment.setStatus(PaymentStatus.valueOf(paymentStatus));
        }

        payment.setTotalAmount(rs.getBigDecimal("total_amount"));
        payment.setCommissionAmount(rs.getBigDecimal("commission_amount"));
        payment.setSellerPayout(rs.getBigDecimal("seller_payout"));
        payment.setPaymentDateTime(getNullableLocalDateTime(rs, "payment_date_time"));
        payment.setCreatedAt(getNullableLocalDateTime(rs, "created_at"));
        payment.setUpdatedAt(getNullableLocalDateTime(rs, "updated_at"));

        return payment;
    }

    private WonAuctionDto mapWonAuctionDto(ResultSet rs) throws Exception {
        String auctionStatusText = rs.getString("auction_status");
        String paymentStatusText = rs.getString("payment_status");

        AuctionStatus auctionStatus = auctionStatusText == null
            ? null
            : AuctionStatus.valueOf(auctionStatusText);

        PaymentStatus paymentStatus = paymentStatusText == null
            ? null
            : PaymentStatus.valueOf(paymentStatusText);

        return new WonAuctionDto(
            rs.getInt("auction_id"),
            rs.getInt("item_id"),
            getNullableInt(rs, "seller_id"),
            getNullableInt(rs, "buyer_id"),
            getNullableInt(rs, "payment_id"),

            rs.getString("item_title"),
            rs.getString("seller_username"),
            rs.getString("category_name"),
            rs.getString("primary_image_url"),

            auctionStatus,
            paymentStatus,

            rs.getBigDecimal("final_sale_price"),
            rs.getBigDecimal("total_amount"),
            rs.getBigDecimal("commission_amount"),
            rs.getBigDecimal("seller_payout"),

            getNullableLocalDateTime(rs, "won_date_time"),
            getNullableLocalDateTime(rs, "payment_date_time")
        );
    }

    private Integer getNullableInt(ResultSet rs, String columnName) throws Exception {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime getNullableLocalDateTime(ResultSet rs, String columnName) throws Exception {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
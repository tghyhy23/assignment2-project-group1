package com.group01.asm2.seeds;

import com.group01.asm2.enums.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PaymentSeed implements Seeder {

    @Override
    public void seed(Connection conn) throws Exception {
        insertPayment(
            conn,
            "Vintage Film Camera",
            "buyer2",
            "seller1",
            BigDecimal.valueOf(155),
            BigDecimal.valueOf(11.63),
            PaymentStatus.PENDING,
            null
        );

        insertPayment(
            conn,
            "Gaming Laptop",
            "buyer2",
            "seller1",
            BigDecimal.valueOf(1050),
            BigDecimal.valueOf(52.50),
            PaymentStatus.PENDING,
            null
        );

        insertPayment(
            conn,
            "Mountain Bike",
            "buyer1",
            "seller1",
            BigDecimal.valueOf(250),
            BigDecimal.valueOf(13.75),
            PaymentStatus.FAILED,
            LocalDateTime.now().minusHours(4)
        );

        insertPayment(
            conn,
            "Mechanical Keyboard",
            "buyer1",
            "seller1",
            BigDecimal.valueOf(110),
            BigDecimal.valueOf(5.50),
            PaymentStatus.COMPLETED,
            LocalDateTime.now().minusDays(1)
        );

        insertPayment(
            conn,
            "Wooden Coffee Table",
            "buyer2",
            "seller1",
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(6.75),
            PaymentStatus.CANCELLED,
            LocalDateTime.now().minusHours(10)
        );
    }

    private void insertPayment(
        Connection conn,
        String itemTitle,
        String buyerUsername,
        String sellerUsername,
        BigDecimal totalAmount,
        BigDecimal commissionAmount,
        PaymentStatus status,
        LocalDateTime paymentDateTime
    ) throws Exception {
        String sql = """
            INSERT INTO payments (
                auction_id,
                buyer_id,
                seller_id,
                total_amount,
                commission_amount,
                seller_payout,
                status,
                payment_date_time
            )
            SELECT a.id, buyer.id, seller.id, ?, ?, ?, ?, ?
            FROM auctions a
            JOIN items i ON a.item_id = i.id
            JOIN persons buyer ON LOWER(buyer.username) = LOWER(?)
            JOIN persons seller ON LOWER(seller.username) = LOWER(?)
            WHERE LOWER(i.title) = LOWER(?)
              AND NOT EXISTS (
                  SELECT 1
                  FROM payments p
                  WHERE p.auction_id = a.id
              )
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            BigDecimal sellerPayout = totalAmount.subtract(commissionAmount);

            ps.setBigDecimal(1, totalAmount);
            ps.setBigDecimal(2, commissionAmount);
            ps.setBigDecimal(3, sellerPayout);
            ps.setString(4, status.name());

            if (paymentDateTime == null) {
                ps.setTimestamp(5, null);
            } else {
                ps.setTimestamp(5, Timestamp.valueOf(paymentDateTime));
            }

            ps.setString(6, buyerUsername.trim().toLowerCase());
            ps.setString(7, sellerUsername.trim().toLowerCase());
            ps.setString(8, itemTitle.trim());

            ps.executeUpdate();
        }
    }
}
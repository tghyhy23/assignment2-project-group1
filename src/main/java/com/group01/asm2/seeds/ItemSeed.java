package com.group01.asm2.seeds;

import com.group01.asm2.enums.ItemCondition;
import com.group01.asm2.enums.ItemStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ItemSeed implements Seeder {
    @Override
    public void seed(Connection conn) throws Exception {
        insertItem(
            conn,
            "Vintage Film Camera",
            "A classic 35mm film camera in good working condition.",
            "Collectibles",
            "seller1",
            BigDecimal.valueOf(120),
            BigDecimal.valueOf(180),
            ItemCondition.USED,
            ItemStatus.ACTIVE
        );

        insertItem(
            conn,
            "Gaming Laptop",
            "A powerful gaming laptop with dedicated graphics card.",
            "Electronics",
            "seller1",
            BigDecimal.valueOf(900),
            BigDecimal.valueOf(1200),
            ItemCondition.USED,
            ItemStatus.ACTIVE
        );

        insertItem(
            conn,
            "Mountain Bike",
            "A durable mountain bike suitable for city and trail riding.",
            "Sports",
            "seller1",
            BigDecimal.valueOf(250),
            BigDecimal.valueOf(350),
            ItemCondition.USED,
            ItemStatus.ACTIVE
        );

        insertItem(
            conn,
            "Mechanical Keyboard",
            "A compact mechanical keyboard with tactile switches.",
            "Electronics",
            "seller1",
            BigDecimal.valueOf(70),
            BigDecimal.valueOf(100),
            ItemCondition.NEW,
            ItemStatus.ACTIVE
        );

        insertItem(
            conn,
            "Wooden Coffee Table",
            "A simple wooden coffee table for living room decoration.",
            "Home & Living",
            "seller1",
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(220),
            ItemCondition.REFURBISHED,
            ItemStatus.ACTIVE
        );
    }

    private void insertItem(
        Connection conn,
        String title,
        String description,
        String categoryName,
        String sellerUsername,
        BigDecimal startingPrice,
        BigDecimal reservePrice,
        ItemCondition condition,
        ItemStatus status
    ) throws Exception {
        String sql = """
            INSERT INTO items (
                title,
                description,
                category_id,
                seller_id,
                starting_price,
                reserve_price,
                condition,
                status
            )
            SELECT ?, ?, c.id, p.id, ?, ?, ?, ?
            FROM categories c
            JOIN persons p ON LOWER(p.username) = LOWER(?)
            WHERE LOWER(c.name) = LOWER(?)
              AND NOT EXISTS (
                  SELECT 1
                  FROM items i
                  WHERE LOWER(i.title) = LOWER(?)
                    AND i.seller_id = p.id
              )
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String normalizedTitle = title.trim();
            String normalizedCategoryName = categoryName.trim();
            String normalizedSellerUsername = sellerUsername.trim().toLowerCase();

            ps.setString(1, normalizedTitle);
            ps.setString(2, description);
            ps.setBigDecimal(3, startingPrice);
            ps.setBigDecimal(4, reservePrice);
            ps.setString(5, condition.name());
            ps.setString(6, status.name());

            ps.setString(7, normalizedSellerUsername);
            ps.setString(8, normalizedCategoryName);
            ps.setString(9, normalizedTitle);

            ps.executeUpdate();
        }
    }
}
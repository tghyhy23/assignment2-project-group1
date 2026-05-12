package com.group01.asm2.seeds;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CategorySeed implements Seeder {

    @Override
    public void seed(Connection conn) throws Exception {
        insertCategory(
            conn,
            "Electronics",
            "Electronic devices, gadgets, and accessories.",
            BigDecimal.valueOf(5.00)
        );

        insertCategory(
            conn,
            "Collectibles",
            "Rare, vintage, and collectible items.",
            BigDecimal.valueOf(7.50)
        );

        insertCategory(
            conn,
            "Fashion",
            "Clothing, shoes, bags, and personal fashion items.",
            BigDecimal.valueOf(6.00)
        );

        insertCategory(
            conn,
            "Home & Living",
            "Furniture, decorations, and household items.",
            BigDecimal.valueOf(4.50)
        );

        insertCategory(
            conn,
            "Sports",
            "Sports equipment, outdoor gear, and fitness items.",
            BigDecimal.valueOf(5.50)
        );
    }

    private void insertCategory(
        Connection conn,
        String name,
        String description,
        BigDecimal commissionRate
    ) throws Exception {
        String sql = """
            INSERT INTO categories (
                name,
                description,
                commission_rate
            )
            SELECT ?, ?, ?
            WHERE NOT EXISTS (
                SELECT 1
                FROM categories
                WHERE LOWER(name) = LOWER(?)
            )
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String normalizedName = name.trim();

            ps.setString(1, normalizedName);
            ps.setString(2, description);
            ps.setBigDecimal(3, commissionRate);

            ps.setString(4, normalizedName);

            ps.executeUpdate();
        }
    }
}
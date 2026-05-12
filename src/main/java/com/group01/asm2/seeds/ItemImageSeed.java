package com.group01.asm2.seeds;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ItemImageSeed implements Seeder {
    @Override
    public void seed(Connection conn) throws Exception {
        insertItemImage(
            conn,
            "Vintage Film Camera",
            "https://res.cloudinary.com/demo/image/upload/sample-camera-1.jpg",
            0
        );

        insertItemImage(
            conn,
            "Vintage Film Camera",
            "https://res.cloudinary.com/demo/image/upload/sample-camera-2.jpg",
            1
        );

        insertItemImage(
            conn,
            "Gaming Laptop",
            "https://res.cloudinary.com/demo/image/upload/sample-laptop-1.jpg",
            0
        );

        insertItemImage(
            conn,
            "Gaming Laptop",
            "https://res.cloudinary.com/demo/image/upload/sample-laptop-2.jpg",
            1
        );

        insertItemImage(
            conn,
            "Mountain Bike",
            "https://res.cloudinary.com/demo/image/upload/sample-bike-1.jpg",
            0
        );

        insertItemImage(
            conn,
            "Mechanical Keyboard",
            "https://res.cloudinary.com/demo/image/upload/sample-keyboard-1.jpg",
            0
        );

        insertItemImage(
            conn,
            "Wooden Coffee Table",
            "https://res.cloudinary.com/demo/image/upload/sample-table-1.jpg",
            0
        );
    }

    private void insertItemImage(
        Connection conn,
        String itemTitle,
        String imageUrl,
        int displayOrder
    ) throws Exception {
        String sql = """
            INSERT INTO item_images (
                item_id,
                image_url,
                display_order
            )
            SELECT i.id, ?, ?
            FROM items i
            WHERE LOWER(i.title) = LOWER(?)
              AND NOT EXISTS (
                  SELECT 1
                  FROM item_images img
                  WHERE img.item_id = i.id
                    AND img.image_url = ?
              )
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String normalizedItemTitle = itemTitle.trim();

            ps.setString(1, imageUrl);
            ps.setInt(2, displayOrder);

            ps.setString(3, normalizedItemTitle);
            ps.setString(4, imageUrl);

            ps.executeUpdate();
        }
    }
}
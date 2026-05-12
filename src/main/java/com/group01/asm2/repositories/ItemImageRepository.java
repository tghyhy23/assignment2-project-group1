package com.group01.asm2.repositories;

import com.group01.asm2.models.ItemImage;
import com.group01.asm2.db.SqlExecutor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public class ItemImageRepository {
    public ItemImage createItemImage(Connection conn, ItemImage itemImage) {
        String sql = """
            INSERT INTO item_images (
                item_id,
                image_url,
                display_order
            )
            VALUES (?, ?, ?)
            RETURNING *
            """;

        return SqlExecutor.queryOne(
            conn,
            sql,
            ps -> {
                ps.setInt(1, itemImage.getItemId());
                ps.setString(2, itemImage.getImageUrl());
                ps.setInt(3, itemImage.getDisplayOrder() != null ? itemImage.getDisplayOrder() : 0);
            },
            this::mapRow
        ).orElse(null);
    }

    public List<ItemImage> createItemImages(Connection conn, List<ItemImage> itemImages) {
        if (itemImages == null || itemImages.isEmpty()) {
            return List.of();
        }

        return itemImages.stream()
            .map(image -> createItemImage(conn, image))
            .toList();
    }

    public List<ItemImage> readItemImagesByItemId(Integer itemId) {
        String sql = """
            SELECT *
            FROM item_images
            WHERE item_id = ?
            ORDER BY display_order ASC, id ASC
            """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, itemId),
            this::mapRow
        );
    }

    public List<ItemImage> readItemImagesByItemId(Connection conn, Integer itemId) {
        String sql = """
            SELECT *
            FROM item_images
            WHERE item_id = ?
            ORDER BY display_order ASC, id ASC
            """;

        return SqlExecutor.queryMany(
            conn,
            sql,
            ps -> ps.setInt(1, itemId),
            this::mapRow
        );
    }

    public void deleteItemImagesByItemId(Connection conn, Integer itemId) {
        String sql = """
            DELETE FROM item_images
            WHERE item_id = ?
            """;

        SqlExecutor.update(
            conn,
            sql,
            ps -> ps.setInt(1, itemId)
        );
    }

    private ItemImage mapRow(ResultSet rs) throws Exception {
        return new ItemImage(
            rs.getInt("id"),
            rs.getInt("item_id"),
            rs.getString("image_url"),
            rs.getInt("display_order")
        );
    }
}
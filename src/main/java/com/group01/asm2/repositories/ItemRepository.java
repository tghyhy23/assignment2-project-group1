package com.group01.asm2.repositories;

import com.group01.asm2.db.SqlExecutor;
import com.group01.asm2.enums.ItemCondition;
import com.group01.asm2.enums.ItemStatus;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Item;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

public class ItemRepository {
    public Item createItem(Item item) {
        String sql = getCreateItemSql();

        return SqlExecutor.queryOne(
            sql,
            ps -> bindCreateItem(ps, item),
            this::mapItem
        ).orElseThrow(() -> AppException.database("Failed to create item."));
    }

    public Item createItem(Connection conn, Item item) {
        String sql = getCreateItemSql();

        return SqlExecutor.queryOne(
            conn,
            sql,
            ps -> bindCreateItem(ps, item),
            this::mapItem
        ).orElseThrow(() -> AppException.database("Failed to create item."));
    }

    public Item readItemById(Integer id) {
        String sql = """
            SELECT id, title, description, category_id, seller_id,
                   starting_price, reserve_price, condition,
                   status, created_at, updated_at
            FROM items
            WHERE id = ?
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, id),
            this::mapItem
        ).orElse(null);
    }

    public List<Item> readItems() {
        String sql = """
            SELECT id, title, description, category_id, seller_id,
                   starting_price, reserve_price, condition,
                   status, created_at, updated_at
            FROM items
            ORDER BY created_at DESC, id DESC
            """;

        return SqlExecutor.queryMany(
            sql,
            ps -> {
            },
            this::mapItem
        );
    }

    public List<Item> readActiveItems() {
        String sql = """
            SELECT id, title, description, category_id, seller_id,
                   starting_price, reserve_price, condition,
                   status, created_at, updated_at
            FROM items
            WHERE status = 'ACTIVE'
            ORDER BY created_at DESC, id DESC
            """;

        return SqlExecutor.queryMany(
            sql,
            ps -> {
            },
            this::mapItem
        );
    }

    public List<Item> readItemsBySellerId(Integer sellerId) {
        String sql = """
            SELECT id, title, description, category_id, seller_id,
                   starting_price, reserve_price, condition,
                   status, created_at, updated_at
            FROM items
            WHERE seller_id = ?
            ORDER BY created_at DESC, id DESC
            """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, sellerId),
            this::mapItem
        );
    }

    public Item updateItem(Item item) {
        String sql = getUpdateItemSql();

        return SqlExecutor.queryOne(
            sql,
            ps -> bindUpdateItem(ps, item),
            this::mapItem
        ).orElse(null);
    }

    public Item updateItem(Connection conn, Item item) {
        String sql = getUpdateItemSql();

        return SqlExecutor.queryOne(
            conn,
            sql,
            ps -> bindUpdateItem(ps, item),
            this::mapItem
        ).orElse(null);
    }

    public void deleteItem(Integer id) {
        String sql = """
            DELETE FROM items
            WHERE id = ?
            """;

        SqlExecutor.update(
            sql,
            ps -> ps.setInt(1, id)
        );
    }

    public void deleteItem(Connection conn, Integer id) {
        String sql = """
            DELETE FROM items
            WHERE id = ?
            """;

        SqlExecutor.update(
            conn,
            sql,
            ps -> ps.setInt(1, id)
        );
    }

    public boolean existsById(Integer id) {
        String sql = """
            SELECT 1
            FROM items
            WHERE id = ?
            LIMIT 1
            """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, id),
            rs -> true
        ).orElse(false);
    }

    private String getCreateItemSql() {
        return """
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
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, title, description, category_id, seller_id,
                      starting_price, reserve_price, condition,
                      status, created_at, updated_at
            """;
    }

    private String getUpdateItemSql() {
        return """
            UPDATE items
            SET title = ?,
                description = ?,
                category_id = ?,
                starting_price = ?,
                reserve_price = ?,
                condition = ?,
                status = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING id, title, description, category_id, seller_id,
                      starting_price, reserve_price, condition,
                      status, created_at, updated_at
            """;
    }

    private void bindCreateItem(java.sql.PreparedStatement ps, Item item) throws Exception {
        ps.setString(1, item.getTitle());
        ps.setString(2, item.getDescription());
        ps.setInt(3, item.getCategoryId());
        ps.setInt(4, item.getSellerId());
        ps.setBigDecimal(5, item.getStartingPrice());

        if (item.getReservePrice() == null) {
            ps.setNull(6, Types.NUMERIC);
        } else {
            ps.setBigDecimal(6, item.getReservePrice());
        }

        ps.setString(7, item.getCondition().name());
        ps.setString(8, item.getStatus().name());
    }

    private void bindUpdateItem(java.sql.PreparedStatement ps, Item item) throws Exception {
        ps.setString(1, item.getTitle());
        ps.setString(2, item.getDescription());
        ps.setInt(3, item.getCategoryId());
        ps.setBigDecimal(4, item.getStartingPrice());

        if (item.getReservePrice() == null) {
            ps.setNull(5, Types.NUMERIC);
        } else {
            ps.setBigDecimal(5, item.getReservePrice());
        }

        ps.setString(6, item.getCondition().name());
        ps.setString(7, item.getStatus().name());
        ps.setInt(8, item.getId());
    }

    private Item mapItem(ResultSet rs) throws Exception {
        Item item = new Item();

        item.setId(rs.getInt("id"));
        item.setTitle(rs.getString("title"));
        item.setDescription(rs.getString("description"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setSellerId(rs.getInt("seller_id"));
        item.setStartingPrice(rs.getBigDecimal("starting_price"));
        item.setReservePrice(rs.getBigDecimal("reserve_price"));

        String condition = rs.getString("condition");
        if (condition != null) {
            item.setCondition(ItemCondition.valueOf(condition));
        }

        String status = rs.getString("status");
        if (status != null) {
            item.setStatus(ItemStatus.valueOf(status));
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            item.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return item;
    }
}
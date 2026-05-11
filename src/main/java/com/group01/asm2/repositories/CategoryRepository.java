package com.group01.asm2.repositories;

import com.group01.asm2.db.SqlExecutor;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Category;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

public class CategoryRepository {

    public Category createCategory(Category category) {
        String sql = """
            INSERT INTO categories (name, description, commission_rate)
            VALUES (?, ?, ?)
            RETURNING id, name, description, commission_rate, created_at, updated_at
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, category.getName());
                ps.setString(2, category.getDescription());
                ps.setBigDecimal(3, category.getCommissionRate());
            },
            this::mapCategory
        ).orElseThrow(() -> AppException.database("Failed to create category."));
    }

    public Category readCategoryById(Integer id) {
        String sql = """
            SELECT id, name, description, commission_rate, created_at, updated_at
            FROM categories
            WHERE id = ?
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, id),
            this::mapCategory
        ).orElse(null);
    }

    public List<Category> readCategories() {
        String sql = """
            SELECT id, name, description, commission_rate, created_at, updated_at
            FROM categories
            ORDER BY name ASC
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> {
            },
            this::mapCategory
        );
    }

    public Category updateCategory(Category category) {
        String sql = """
            UPDATE categories
            SET name = ?,
                description = ?,
                commission_rate = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING id, name, description, commission_rate, created_at, updated_at
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, category.getName());
                ps.setString(2, category.getDescription());
                ps.setBigDecimal(3, category.getCommissionRate());
                ps.setInt(4, category.getId());
            },
            this::mapCategory
        ).orElse(null);
    }

    public void deleteCategory(Integer id) {
        String sql = """
            DELETE FROM categories
            WHERE id = ?
        """;

        SqlExecutor.update(
            sql,
            ps -> ps.setInt(1, id)
        );
    }

    public boolean existsByNameExceptId(String name, Integer excludedCategoryId) {
        String sql = """
            SELECT 1
            FROM categories
            WHERE LOWER(name) = LOWER(?)
              AND (? IS NULL OR id <> ?)
            LIMIT 1
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, name);

                if (excludedCategoryId == null) {
                    ps.setNull(2, Types.INTEGER);
                    ps.setNull(3, Types.INTEGER);
                } else {
                    ps.setInt(2, excludedCategoryId);
                    ps.setInt(3, excludedCategoryId);
                }
            },
            rs -> true
        ).orElse(false);
    }

    public boolean isCategoryUsedByItems(Integer categoryId) {
        String sql = """
            SELECT 1
            FROM items
            WHERE category_id = ?
            LIMIT 1
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, categoryId),
            rs -> true
        ).orElse(false);
    }

    private Category mapCategory(ResultSet rs) throws Exception {
        Category category = new Category();

        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setCommissionRate(rs.getBigDecimal("commission_rate"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        if (createdAt != null) {
            category.setCreatedAt(createdAt.toLocalDateTime());
        }

        if (updatedAt != null) {
            category.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return category;
    }
}
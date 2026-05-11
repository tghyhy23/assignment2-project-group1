package com.group01.asm2.repositories;

import com.group01.asm2.configs.DatabaseConfig;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Group 01
 */
public class CategoryRepository {

    public Category createCategory(Category category) {
        String sql = """
            INSERT INTO categories (name, description, commission_rate)
            VALUES (?, ?, ?)
            RETURNING id, name, description, commission_rate, created_at, updated_at
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setBigDecimal(3, category.getCommissionRate());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }

            throw AppException.database("Failed to create category.");

        } catch (SQLException e) {
            throw AppException.database("Database error while creating category: " + e.getMessage());
        }
    }

    public Category readCategoryById(Integer id) {
        String sql = """
            SELECT id, name, description, commission_rate, created_at, updated_at
            FROM categories
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw AppException.database("Database error while reading category: " + e.getMessage());
        }
    }

    public List<Category> readCategories() {
        String sql = """
            SELECT id, name, description, commission_rate, created_at, updated_at
            FROM categories
            ORDER BY name ASC
        """;

        List<Category> categories = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapCategory(rs));
            }

            return categories;

        } catch (SQLException e) {
            throw AppException.database("Database error while reading categories: " + e.getMessage());
        }
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

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setBigDecimal(3, category.getCommissionRate());
            stmt.setInt(4, category.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw AppException.database("Database error while updating category: " + e.getMessage());
        }
    }

    public void deleteCategory(Integer id) {
        String sql = """
            DELETE FROM categories
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw AppException.database("Database error while deleting category: " + e.getMessage());
        }
    }

    public boolean existsByNameExceptId(String name, Integer excludedCategoryId) {
        String sql = """
            SELECT 1
            FROM categories
            WHERE LOWER(name) = LOWER(?)
              AND (? IS NULL OR id <> ?)
            LIMIT 1
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            if (excludedCategoryId == null) {
                stmt.setNull(2, Types.INTEGER);
                stmt.setNull(3, Types.INTEGER);
            } else {
                stmt.setInt(2, excludedCategoryId);
                stmt.setInt(3, excludedCategoryId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw AppException.database("Database error while checking category name: " + e.getMessage());
        }
    }

    public boolean isCategoryUsedByItems(Integer categoryId) {
        String sql = """
            SELECT 1
            FROM items
            WHERE category_id = ?
            LIMIT 1
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw AppException.database("Database error while checking category usage: " + e.getMessage());
        }
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
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
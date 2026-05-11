package com.group01.asm2.repositories;

import com.group01.asm2.db.SqlExecutor;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.User;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

public class UserRepository {
    public User createUser(User user) {
        String sql = """
            INSERT INTO persons (
                full_name,
                date_of_birth,
                email,
                phone,
                username,
                password,
                role,
                balance,
                rating,
                completed_sales_count,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            RETURNING id,
                      full_name,
                      date_of_birth,
                      email,
                      phone,
                      username,
                      password,
                      role,
                      created_at,
                      updated_at,
                      balance,
                      rating,
                      completed_sales_count
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, user.getFullName());

                if (user.getDateOfBirth() == null) {
                    ps.setNull(2, Types.DATE);
                } else {
                    ps.setDate(2, Date.valueOf(user.getDateOfBirth()));
                }

                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhone());
                ps.setString(5, user.getUsername());
                ps.setString(6, user.getPassword());
                ps.setString(7, user.getRole().name());
                ps.setBigDecimal(8, user.getBalance());
                ps.setDouble(9, user.getRating());
                ps.setInt(10, user.getCompletedSalesCount());
            },
            this::mapRowToUser
        ).orElseThrow(() -> AppException.database("Could not create user."));
    }

    public User readUserById(Integer id) {
        String sql = """
            SELECT id,
                   full_name,
                   date_of_birth,
                   email,
                   phone,
                   username,
                   password,
                   role,
                   created_at,
                   updated_at,
                   balance,
                   rating,
                   completed_sales_count
            FROM persons
            WHERE id = ?
              AND role IN ('BUYER', 'SELLER')
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, id),
            this::mapRowToUser
        ).orElse(null);
    }

    public List<User> readUsers() {
        String sql = """
            SELECT id,
                   full_name,
                   date_of_birth,
                   email,
                   phone,
                   username,
                   password,
                   role,
                   created_at,
                   updated_at,
                   balance,
                   rating,
                   completed_sales_count
            FROM persons
            WHERE role IN ('BUYER', 'SELLER')
            ORDER BY id ASC
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> {
            },
            this::mapRowToUser
        );
    }

    public User updateUserProfile(User user) {
        String sql = """
            UPDATE persons
            SET full_name = ?,
                date_of_birth = ?,
                email = ?,
                phone = ?,
                username = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND role IN ('BUYER', 'SELLER')
            RETURNING id,
                      full_name,
                      date_of_birth,
                      email,
                      phone,
                      username,
                      password,
                      role,
                      created_at,
                      updated_at,
                      balance,
                      rating,
                      completed_sales_count
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, user.getFullName());

                if (user.getDateOfBirth() == null) {
                    ps.setNull(2, Types.DATE);
                } else {
                    ps.setDate(2, Date.valueOf(user.getDateOfBirth()));
                }

                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhone());
                ps.setString(5, user.getUsername());
                ps.setInt(6, user.getId());
            },
            this::mapRowToUser
        ).orElseThrow(() -> AppException.notFound("User profile not found."));
    }

    public int deleteUser(Integer id) {
        String sql = """
            DELETE FROM persons
            WHERE id = ?
              AND role IN ('BUYER', 'SELLER')
        """;

        return SqlExecutor.update(
            sql,
            ps -> ps.setInt(1, id)
        );
    }

    public boolean existsByUsernameExceptId(String username, Integer excludedUserId) {
        String sql = """
            SELECT 1
            FROM persons
            WHERE LOWER(username) = LOWER(?)
              AND (? IS NULL OR id <> ?)
            LIMIT 1
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, username);

                if (excludedUserId == null) {
                    ps.setNull(2, Types.INTEGER);
                    ps.setNull(3, Types.INTEGER);
                } else {
                    ps.setInt(2, excludedUserId);
                    ps.setInt(3, excludedUserId);
                }
            },
            rs -> true
        ).orElse(false);
    }

    public boolean existsByEmailExceptId(String email, Integer excludedUserId) {
        String sql = """
            SELECT 1
            FROM persons
            WHERE LOWER(email) = LOWER(?)
              AND (? IS NULL OR id <> ?)
            LIMIT 1
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, email);

                if (excludedUserId == null) {
                    ps.setNull(2, Types.INTEGER);
                    ps.setNull(3, Types.INTEGER);
                } else {
                    ps.setInt(2, excludedUserId);
                    ps.setInt(3, excludedUserId);
                }
            },
            rs -> true
        ).orElse(false);
    }

    private User mapRowToUser(ResultSet resultSet) throws Exception {
        return new User(
            resultSet.getInt("id"),
            resultSet.getString("full_name"),
            resultSet.getDate("date_of_birth") == null
                ? null
                : resultSet.getDate("date_of_birth").toLocalDate(),
            resultSet.getString("email"),
            resultSet.getString("phone"),
            resultSet.getString("username"),
            resultSet.getString("password"),
            UserRole.valueOf(resultSet.getString("role")),
            getLocalDateTime(resultSet, "created_at"),
            getLocalDateTime(resultSet, "updated_at"),
            resultSet.getBigDecimal("balance"),
            resultSet.getDouble("rating"),
            resultSet.getInt("completed_sales_count")
        );
    }

    private java.time.LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws Exception {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
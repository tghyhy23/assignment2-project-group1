package com.group01.asm2.repositories;

import com.group01.asm2.configs.DatabaseConfig;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.models.Person;
import com.group01.asm2.models.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class PersonRepository {
    public Optional<Person> findByUsername(String username) {
        String sql = """
            SELECT
                id,
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
            WHERE LOWER(username) = LOWER(?)
            LIMIT 1
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapResultSetToPerson(rs));
            }

        } catch (Exception exception) {
            throw new RuntimeException("Failed to find person by username: " + exception.getMessage(), exception);
        }
    }

    private Person mapResultSetToPerson(ResultSet rs) throws Exception {
        Integer id = rs.getInt("id");
        String fullName = rs.getString("full_name");
        LocalDate dateOfBirth = getLocalDate(rs, "date_of_birth");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password");
        UserRole role = UserRole.valueOf(rs.getString("role"));
        LocalDateTime createdAt = getLocalDateTime(rs, "created_at");
        LocalDateTime updatedAt = getLocalDateTime(rs, "updated_at");

        if (role == UserRole.BUYER || role == UserRole.SELLER) {
            BigDecimal balance = rs.getBigDecimal("balance");
            double rating = rs.getDouble("rating");
            int completedSalesCount = rs.getInt("completed_sales_count");

            return new User(
                id,
                fullName,
                dateOfBirth,
                email,
                phone,
                username,
                passwordHash,
                role,
                createdAt,
                updatedAt,
                balance,
                rating,
                completedSalesCount
            );
        }

        return new Person(
            id,
            fullName,
            dateOfBirth,
            email,
            phone,
            username,
            passwordHash,
            role,
            createdAt,
            updatedAt
        );
    }

    private LocalDate getLocalDate(ResultSet rs, String columnName) throws Exception {
        Date date = rs.getDate(columnName);
        return date == null ? null : date.toLocalDate();
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws Exception {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
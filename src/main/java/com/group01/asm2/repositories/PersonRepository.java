package com.group01.asm2.repositories;

import com.group01.asm2.db.SqlExecutor;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.models.Person;
import com.group01.asm2.models.User;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author Group 01
 */
public class PersonRepository {

    public Optional<Person> findByUsername(String username) {
        String sql = """
            SELECT
                id,
                full_name,
                date_of_birth,
                email,
                phone,
                address,
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

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setString(1, username),
            this::mapResultSetToPerson
        );
    }

    private Person mapResultSetToPerson(ResultSet rs) throws Exception {
        Integer id = rs.getInt("id");
        String fullName = rs.getString("full_name");
        LocalDate dateOfBirth = getLocalDate(rs, "date_of_birth");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
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
                address,
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
            address,
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
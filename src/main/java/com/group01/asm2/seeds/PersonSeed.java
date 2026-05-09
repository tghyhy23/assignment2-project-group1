package com.group01.asm2.seeds;

import com.group01.asm2.enums.UserRole;
import com.group01.asm2.utils.PasswordHasher;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class PersonSeed implements Seeder {

    private static final String DEFAULT_PASSWORD = "Bidbliztapp@123";

    @Override
    public void seed(Connection conn) throws Exception {
        insertPerson(
            conn,
            "Buyer One",
            LocalDate.of(2000, 1, 15),
            "buyer1@bidblitz.com",
            "0900000001",
            "buyer1",
            UserRole.BUYER,
            BigDecimal.valueOf(1500),
            4.7,
            0
        );

        insertPerson(
            conn,
            "Buyer Two",
            LocalDate.of(2001, 5, 20),
            "buyer2@bidblitz.com",
            "0900000002",
            "buyer2",
            UserRole.BUYER,
            BigDecimal.valueOf(2500),
            4.3,
            0
        );

        insertPerson(
            conn,
            "Seller One",
            LocalDate.of(1998, 8, 10),
            "seller1@bidblitz.com",
            "0900000003",
            "seller1",
            UserRole.SELLER,
            BigDecimal.valueOf(500),
            4.8,
            12
        );

        insertPerson(
            conn,
            "Auction Admin",
            LocalDate.of(1995, 3, 12),
            "auctionadmin@bidblitz.com",
            "0900000004",
            "auctionadmin",
            UserRole.AUCTION_ADMINISTRATOR,
            BigDecimal.ZERO,
            0.0,
            0
        );

        insertPerson(
            conn,
            "System Admin",
            LocalDate.of(1992, 11, 2),
            "systemadmin@bidblitz.com",
            "0900000005",
            "systemadmin",
            UserRole.SYSTEM_ADMINISTRATOR,
            BigDecimal.ZERO,
            0.0,
            0
        );
    }

    private void insertPerson(
        Connection conn,
        String fullName,
        LocalDate dateOfBirth,
        String email,
        String phone,
        String username,
        UserRole role,
        BigDecimal balance,
        double rating,
        int completedSalesCount
    ) throws Exception {
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
                completed_sales_count
            )
            SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            WHERE NOT EXISTS (
                SELECT 1
                FROM persons
                WHERE LOWER(username) = LOWER(?)
                   OR LOWER(email) = LOWER(?)
            )
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String normalizedEmail = email.trim().toLowerCase();
            String normalizedUsername = username.trim().toLowerCase();

            ps.setString(1, fullName);
            ps.setDate(2, Date.valueOf(dateOfBirth));
            ps.setString(3, normalizedEmail);
            ps.setString(4, phone);
            ps.setString(5, normalizedUsername);
            ps.setString(6, PasswordHasher.hash(DEFAULT_PASSWORD));
            ps.setString(7, role.name());
            ps.setBigDecimal(8, balance);
            ps.setDouble(9, rating);
            ps.setInt(10, completedSalesCount);

            ps.setString(11, normalizedUsername);
            ps.setString(12, normalizedEmail);

            ps.executeUpdate();
        }
    }
}
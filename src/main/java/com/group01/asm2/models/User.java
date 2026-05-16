package com.group01.asm2.models;

import com.group01.asm2.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class User extends Person {
    private BigDecimal balance;
    private double rating;
    private int completedSalesCount;

    public User() {
        super();
        setRole(UserRole.BUYER);
        this.balance = BigDecimal.ZERO;
        this.rating = 0.0;
        this.completedSalesCount = 0;
    }

    public User(Integer id, String fullName, LocalDate dateOfBirth, String email, String phone, String address,
                String username, String password, UserRole role,
                LocalDateTime createdAt, LocalDateTime updatedAt,
                BigDecimal balance, double rating, int completedSalesCount) {
        super(id, fullName, dateOfBirth, email, phone, address, username, password,
            validateUserRole(role), createdAt, updatedAt);

        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.rating = rating;
        this.completedSalesCount = completedSalesCount;
    }

    private static UserRole validateUserRole(UserRole role) {
        if (role == null) {
            return UserRole.BUYER;
        }

        if (role != UserRole.BUYER && role != UserRole.SELLER) {
            throw new IllegalArgumentException("User role must be BUYER or SELLER.");
        }

        return role;
    }

    @Override
    public void setRole(UserRole role) {
        super.setRole(validateUserRole(role));
    }

    public boolean hasEnoughBalance(BigDecimal amount) {
        if (amount == null || balance == null) {
            return false;
        }

        return balance.compareTo(amount) >= 0;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getCompletedSalesCount() {
        return completedSalesCount;
    }

    public void setCompletedSalesCount(int completedSalesCount) {
        this.completedSalesCount = completedSalesCount;
    }
}
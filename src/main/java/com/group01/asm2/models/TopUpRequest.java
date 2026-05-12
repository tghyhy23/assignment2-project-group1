package com.group01.asm2.models;

import java.time.LocalDateTime;

public class TopUpRequest {
    private final int userId;
    private final double amount;
    private final String status;
    private final LocalDateTime createdAt;

    public TopUpRequest(int userId, double amount) {
        this.userId = userId;
        this.amount = amount;
        this.status = "Pending";
        this.createdAt = LocalDateTime.now();
    }

    public int getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
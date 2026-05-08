package com.group01.asm2.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Category {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal commissionRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Category() {
    }

    public Category(Integer id, String name, String description, BigDecimal commissionRate,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.commissionRate = commissionRate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public BigDecimal calculateCommission(BigDecimal amount) {
        if (amount == null || commissionRate == null) {
            return BigDecimal.ZERO;
        }

        return amount.multiply(commissionRate).divide(BigDecimal.valueOf(100));
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
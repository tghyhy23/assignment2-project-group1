package com.group01.asm2.models;

import com.group01.asm2.enums.ItemCondition;
import com.group01.asm2.enums.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Item {
    private Integer id;
    private String title;
    private String description;
    private Integer categoryId;
    private Integer sellerId;
    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private ItemCondition condition;
    private String imageUrl;
    private ItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Item() {
    }

    public Item(Integer id, String title, String description, Integer categoryId, Integer sellerId,
                BigDecimal startingPrice, BigDecimal reservePrice, ItemCondition condition,
                String imageUrl, ItemStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.condition = condition;
        this.imageUrl = imageUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isOwnedBy(Integer userId) {
        return sellerId != null && sellerId.equals(userId);
    }

    public boolean hasReservePrice() {
        return reservePrice != null;
    }

    public boolean isActive() {
        return status == ItemStatus.ACTIVE;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getReservePrice() {
        return reservePrice;
    }

    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
    }

    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
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
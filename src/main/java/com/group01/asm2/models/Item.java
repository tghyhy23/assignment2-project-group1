package com.group01.asm2.models;

import com.group01.asm2.enums.ItemCondition;
import com.group01.asm2.enums.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Item {
    private Integer id;
    private String title;
    private String description;
    private Integer categoryId;
    private Integer sellerId;
    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private ItemCondition condition;
    private List<ItemImage> images;
    private ItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Item() {
        this.images = new ArrayList<>();
    }

    public Item(Integer id, String title, String description, Integer categoryId, Integer sellerId,
                BigDecimal startingPrice, BigDecimal reservePrice, ItemCondition condition,
                ItemStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.condition = condition;
        this.images = new ArrayList<>();
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Item(Integer id, String title, String description, Integer categoryId, Integer sellerId,
                BigDecimal startingPrice, BigDecimal reservePrice, ItemCondition condition,
                List<ItemImage> images, ItemStatus status,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.condition = condition;
        this.images = images != null ? images : new ArrayList<>();
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

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    public ItemImage getPrimaryImage() {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.get(0);
    }

    public String getPrimaryImageUrl() {
        ItemImage primaryImage = getPrimaryImage();
        return primaryImage == null ? null : primaryImage.getImageUrl();
    }

    public void addImage(ItemImage image) {
        if (images == null) {
            images = new ArrayList<>();
        }

        if (image != null) {
            images.add(image);
        }
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

    public List<ItemImage> getImages() {
        return images;
    }

    public void setImages(List<ItemImage> images) {
        this.images = images != null ? images : new ArrayList<>();
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
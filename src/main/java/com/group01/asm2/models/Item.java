package com.group01.asm2.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Item {
    // ---- THÔNG TIN LƯU DATABASE ----
    private Integer id;             // ID chính của sản phẩm
    private Integer categoryId;     // Mối quan hệ với Category
    private Integer sellerId;       // Mối quan hệ với User (Người bán)

    private String title;
    private String description;
    private String condition;       // Ví dụ: "New", "Used", "Like New"
    private String brand;
    private String location;

    private BigDecimal startingPrice;
    private BigDecimal reservePrice; // Giá giấu kín tối thiểu để bán

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---- THÔNG TIN HIỂN THỊ UI (Trang Explore) ----
    private BigDecimal currentBid;   // Giá đấu cao nhất hiện tại (thực tế lấy từ bảng Bid/Auction)
    private int bidCount;            // Số lượt người đã bid
    private boolean isRecommended;   // Cờ đánh dấu sản phẩm nổi bật
    private String mainBgClass;      // Class CSS giả lập ảnh chính
    private String[] thumbBgClasses; // Mảng class CSS giả lập 4 ảnh phụ

    public Item() {
    }

    // Constructor chuẩn cho Database
    public Item(Integer id, Integer categoryId, Integer sellerId, String title,
                String description, String condition, String brand, String location,
                BigDecimal startingPrice, BigDecimal reservePrice,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.condition = condition;
        this.brand = brand;
        this.location = location;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor Mock cho trang Explore (UI)
    public Item(Integer id, String title, BigDecimal startingPrice, BigDecimal currentBid,
                int bidCount, boolean isRecommended, String mainBgClass, String[] thumbBgClasses) {
        this.id = id;
        this.title = title;
        this.startingPrice = startingPrice;
        this.currentBid = currentBid;
        this.bidCount = bidCount;
        this.isRecommended = isRecommended;
        this.mainBgClass = mainBgClass;
        this.thumbBgClasses = thumbBgClasses;
    }

    // ==========================================
    // GETTERS & SETTERS (Tự động sinh hoặc gõ vào)
    // ==========================================
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Integer getSellerId() { return sellerId; }
    public void setSellerId(Integer sellerId) { this.sellerId = sellerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }

    public BigDecimal getReservePrice() { return reservePrice; }
    public void setReservePrice(BigDecimal reservePrice) { this.reservePrice = reservePrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Getters/Setters UI
    public BigDecimal getCurrentBid() { return currentBid; }
    public void setCurrentBid(BigDecimal currentBid) { this.currentBid = currentBid; }

    public int getBidCount() { return bidCount; }
    public void setBidCount(int bidCount) { this.bidCount = bidCount; }

    public boolean isRecommended() { return isRecommended; }
    public void setRecommended(boolean recommended) { isRecommended = recommended; }

    public String getMainBgClass() { return mainBgClass; }
    public void setMainBgClass(String mainBgClass) { this.mainBgClass = mainBgClass; }

    public String[] getThumbBgClasses() { return thumbBgClasses; }
    public void setThumbBgClasses(String[] thumbBgClasses) { this.thumbBgClasses = thumbBgClasses; }
}
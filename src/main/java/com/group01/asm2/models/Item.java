package com.group01.asm2.models;

public class Item {

    private String itemId;
    private String title;

    // private String description;
    // private Category category;

    private double startingPrice;

    // private Double reservePrice;
    // private ItemCondition condition;
    // private User seller;

    private double currentBid;       // Giá đấu cao nhất hiện tại
    private int bidCount;            // Số lượt người đã bid
    private boolean isRecommended;   // Cờ đánh dấu sản phẩm nổi bật
    private String mainBgClass;      // Class CSS giả lập ảnh chính
    private String[] thumbBgClasses; // Mảng class CSS giả lập 4 ảnh phụ

    // ==========================================
    // 3. CONSTRUCTORS
    // ==========================================
    public Item() {
    }

    // Constructor cũ (Tạm comment lại vì chứa các field đã bị ẩn)
    /*
    public Item(String itemId, String title, String description, Category category,
                double startingPrice, Double reservePrice, ItemCondition condition, User seller) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.condition = condition;
        this.seller = seller;
    }
    */

    // Constructor mới dành riêng cho việc tạo Mock Data ở trang Explore
    public Item(String itemId, String title, double startingPrice, double currentBid,
                int bidCount, boolean isRecommended, String mainBgClass, String[] thumbBgClasses) {
        this.itemId = itemId;
        this.title = title;
        this.startingPrice = startingPrice;
        this.currentBid = currentBid;
        this.bidCount = bidCount;
        this.isRecommended = isRecommended;
        this.mainBgClass = mainBgClass;
        this.thumbBgClasses = thumbBgClasses;
    }

    // ==========================================
    // 4. GETTERS & SETTERS
    // ==========================================
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    // --- Getters & Setters cho các field mới ---
    public double getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(double currentBid) {
        this.currentBid = currentBid;
    }

    public int getBidCount() {
        return bidCount;
    }

    public void setBidCount(int bidCount) {
        this.bidCount = bidCount;
    }

    public boolean isRecommended() {
        return isRecommended;
    }

    public void setRecommended(boolean recommended) {
        isRecommended = recommended;
    }

    public String getMainBgClass() {
        return mainBgClass;
    }

    public void setMainBgClass(String mainBgClass) {
        this.mainBgClass = mainBgClass;
    }

    public String[] getThumbBgClasses() {
        return thumbBgClasses;
    }

    public void setThumbBgClasses(String[] thumbBgClasses) {
        this.thumbBgClasses = thumbBgClasses;
    }

    // --- Các Getters & Setters cũ đã comment ---
    /*
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Double getReservePrice() { return reservePrice; }
    public void setReservePrice(Double reservePrice) { this.reservePrice = reservePrice; }

    public ItemCondition getCondition() { return condition; }
    public void setCondition(ItemCondition condition) { this.condition = condition; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
    */
}
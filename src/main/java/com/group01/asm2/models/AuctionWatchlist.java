package com.group01.asm2.models;

import java.time.LocalDateTime;

public class AuctionWatchlist {
    private Integer id;
    private Integer userId;
    private Integer auctionId;
    private LocalDateTime createdAt;

    public AuctionWatchlist() {
    }

    public AuctionWatchlist(Integer id, Integer userId, Integer auctionId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.auctionId = auctionId;
        this.createdAt = createdAt;
    }

    public boolean isOwnedBy(Integer userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    public boolean watchesAuction(Integer auctionId) {
        return this.auctionId != null && this.auctionId.equals(auctionId);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Integer auctionId) {
        this.auctionId = auctionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
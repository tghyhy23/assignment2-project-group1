package com.group01.asm2.models;

import com.group01.asm2.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Auction {
    private Integer id;
    private Integer itemId;
    private AuctionStatus status;
    private Integer currentHighestBidId;
    private Integer winnerId;
    private BigDecimal finalSalePrice;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Auction() {
    }

    public Auction(Integer id, Integer itemId, AuctionStatus status, Integer currentHighestBidId,
                   Integer winnerId, BigDecimal finalSalePrice,
                   LocalDateTime startDateTime, LocalDateTime endDateTime,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.itemId = itemId;
        this.status = status;
        this.currentHighestBidId = currentHighestBidId;
        this.winnerId = winnerId;
        this.finalSalePrice = finalSalePrice;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return status == AuctionStatus.ACTIVE;
    }

    public boolean isEnded() {
        return status == AuctionStatus.ENDED
            || status == AuctionStatus.SOLD
            || status == AuctionStatus.UNSOLD
            || status == AuctionStatus.CANCELLED;
    }

    public boolean isDue(LocalDateTime now) {
        return isActive() && endDateTime != null && !endDateTime.isAfter(now);
    }

    public boolean hasWinner() {
        return winnerId != null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public Integer getCurrentHighestBidId() {
        return currentHighestBidId;
    }

    public void setCurrentHighestBidId(Integer currentHighestBidId) {
        this.currentHighestBidId = currentHighestBidId;
    }

    public Integer getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }

    public BigDecimal getFinalSalePrice() {
        return finalSalePrice;
    }

    public void setFinalSalePrice(BigDecimal finalSalePrice) {
        this.finalSalePrice = finalSalePrice;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
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
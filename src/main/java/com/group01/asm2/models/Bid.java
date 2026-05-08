package com.group01.asm2.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bid {
    private Integer id;
    private Integer auctionId;
    private Integer itemId;
    private Integer bidderId;
    private BigDecimal amount;
    private LocalDateTime bidDateTime;

    public Bid() {
    }

    public Bid(Integer id, Integer auctionId, Integer itemId, Integer bidderId,
               BigDecimal amount, LocalDateTime bidDateTime) {
        this.id = id;
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.bidDateTime = bidDateTime;
    }

    public boolean isHigherThan(BigDecimal otherAmount) {
        if (amount == null) {
            return false;
        }

        if (otherAmount == null) {
            return true;
        }

        return amount.compareTo(otherAmount) > 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Integer auctionId) {
        this.auctionId = auctionId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getBidderId() {
        return bidderId;
    }

    public void setBidderId(Integer bidderId) {
        this.bidderId = bidderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getBidDateTime() {
        return bidDateTime;
    }

    public void setBidDateTime(LocalDateTime bidDateTime) {
        this.bidDateTime = bidDateTime;
    }
}
package com.group01.asm2.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidHistoryViewModel {

    private final Bid bid;
    private final String itemName;
    private final String mainBgClass;
    private final String bidStatus;
    private final String paymentStatus;
    private final BigDecimal myBidAmount;
    private final BigDecimal currentHighestBidAmount;
    private final LocalDateTime bidDateTime;
    private final String finalResultText;

    public BidHistoryViewModel(
            Bid bid,
            String itemName,
            String mainBgClass,
            String bidStatus,
            String paymentStatus,
            BigDecimal myBidAmount,
            BigDecimal currentHighestBidAmount,
            LocalDateTime bidDateTime,
            String finalResultText
    ) {
        this.bid = bid;
        this.itemName = itemName;
        this.mainBgClass = mainBgClass;
        this.bidStatus = bidStatus;
        this.paymentStatus = paymentStatus;
        this.myBidAmount = myBidAmount;
        this.currentHighestBidAmount = currentHighestBidAmount;
        this.bidDateTime = bidDateTime;
        this.finalResultText = finalResultText;
    }

    public Bid getBid() {
        return bid;
    }

    public String getItemName() {
        return itemName;
    }

    public String getMainBgClass() {
        return mainBgClass;
    }

    public String getBidStatus() {
        return bidStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public BigDecimal getMyBidAmount() {
        return myBidAmount;
    }

    public BigDecimal getCurrentHighestBidAmount() {
        return currentHighestBidAmount;
    }

    public LocalDateTime getBidDateTime() {
        return bidDateTime;
    }

    public String getFinalResultText() {
        return finalResultText;
    }

    public boolean canPay() {
        return isWon() && "Pending Payment".equalsIgnoreCase(paymentStatus);
    }

    public boolean canBidAgain() {
        return "Outbid".equalsIgnoreCase(bidStatus);
    }

    public boolean isWinning() {
        return "Winning".equalsIgnoreCase(bidStatus);
    }

    public boolean isWon() {
        return "Won".equalsIgnoreCase(bidStatus);
    }

    public boolean isLost() {
        return "Lost".equalsIgnoreCase(bidStatus);
    }
}
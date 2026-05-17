package com.group01.asm2.dtos.reports;

/**
 * @author Group 01
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SellerActivitySummaryReportDto {
    private final Integer itemId;
    private final Integer auctionId;
    private final String itemTitle;
    private final String categoryName;
    private final String condition;
    private final BigDecimal startingPrice;
    private final BigDecimal reservePrice;
    private final String auctionStatus;
    private final BigDecimal finalSalePrice;
    private final String winnerUsername;
    private final BigDecimal commissionAmount;
    private final BigDecimal sellerPayout;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;

    public SellerActivitySummaryReportDto(
        Integer itemId,
        Integer auctionId,
        String itemTitle,
        String categoryName,
        String condition,
        BigDecimal startingPrice,
        BigDecimal reservePrice,
        String auctionStatus,
        BigDecimal finalSalePrice,
        String winnerUsername,
        BigDecimal commissionAmount,
        BigDecimal sellerPayout,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
    ) {
        this.itemId = itemId;
        this.auctionId = auctionId;
        this.itemTitle = itemTitle;
        this.categoryName = categoryName;
        this.condition = condition;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.auctionStatus = auctionStatus;
        this.finalSalePrice = finalSalePrice;
        this.winnerUsername = winnerUsername;
        this.commissionAmount = commissionAmount;
        this.sellerPayout = sellerPayout;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public Integer getItemId() {
        return itemId;
    }

    public Integer getAuctionId() {
        return auctionId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCondition() {
        return condition;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public BigDecimal getReservePrice() {
        return reservePrice;
    }

    public String getAuctionStatus() {
        return auctionStatus;
    }

    public BigDecimal getFinalSalePrice() {
        return finalSalePrice;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public BigDecimal getSellerPayout() {
        return sellerPayout;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
}
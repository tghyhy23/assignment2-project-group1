package com.group01.asm2.dtos.reports;

/**
 * @author Group 01
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BuyerBiddingHistoryReportDto {
    private final Integer bidId;
    private final Integer auctionId;
    private final String itemTitle;
    private final String categoryName;
    private final BigDecimal bidAmount;
    private final LocalDateTime bidDateTime;
    private final String auctionStatus;
    private final BigDecimal currentHighestBid;
    private final String bidResult;

    public BuyerBiddingHistoryReportDto(
        Integer bidId,
        Integer auctionId,
        String itemTitle,
        String categoryName,
        BigDecimal bidAmount,
        LocalDateTime bidDateTime,
        String auctionStatus,
        BigDecimal currentHighestBid,
        String bidResult
    ) {
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.itemTitle = itemTitle;
        this.categoryName = categoryName;
        this.bidAmount = bidAmount;
        this.bidDateTime = bidDateTime;
        this.auctionStatus = auctionStatus;
        this.currentHighestBid = currentHighestBid;
        this.bidResult = bidResult;
    }

    public Integer getBidId() {
        return bidId;
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

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public LocalDateTime getBidDateTime() {
        return bidDateTime;
    }

    public String getAuctionStatus() {
        return auctionStatus;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public String getBidResult() {
        return bidResult;
    }
}
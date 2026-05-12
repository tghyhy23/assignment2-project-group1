package com.group01.asm2.models;

import java.math.BigDecimal;
import java.util.List;

public class BidResult {
    private final boolean success;
    private final String message;
    private final BigDecimal newHighestBid;
    private final List<AuctionBidEntry> updatedBidHistory;

    public BidResult(
            boolean success,
            String message,
            BigDecimal newHighestBid,
            List<AuctionBidEntry> updatedBidHistory
    ) {
        this.success = success;
        this.message = message;
        this.newHighestBid = newHighestBid;
        this.updatedBidHistory = updatedBidHistory;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getNewHighestBid() {
        return newHighestBid;
    }

    public List<AuctionBidEntry> getUpdatedBidHistory() {
        return updatedBidHistory;
    }
}
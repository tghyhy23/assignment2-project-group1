package com.group01.asm2.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionBidEntry(
        String bidderName,
        BigDecimal amount,
        LocalDateTime bidTime,
        String status
)  {
}
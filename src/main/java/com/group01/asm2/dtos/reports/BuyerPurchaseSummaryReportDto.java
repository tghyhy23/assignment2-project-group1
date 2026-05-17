package com.group01.asm2.dtos.reports;

/**
 * @author Group 01
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BuyerPurchaseSummaryReportDto {
    private final Integer paymentId;
    private final Integer auctionId;
    private final String itemTitle;
    private final String sellerUsername;
    private final BigDecimal finalSalePrice;
    private final BigDecimal commissionAmount;
    private final BigDecimal sellerPayout;
    private final String paymentStatus;
    private final LocalDateTime paymentDateTime;

    public BuyerPurchaseSummaryReportDto(
        Integer paymentId,
        Integer auctionId,
        String itemTitle,
        String sellerUsername,
        BigDecimal finalSalePrice,
        BigDecimal commissionAmount,
        BigDecimal sellerPayout,
        String paymentStatus,
        LocalDateTime paymentDateTime
    ) {
        this.paymentId = paymentId;
        this.auctionId = auctionId;
        this.itemTitle = itemTitle;
        this.sellerUsername = sellerUsername;
        this.finalSalePrice = finalSalePrice;
        this.commissionAmount = commissionAmount;
        this.sellerPayout = sellerPayout;
        this.paymentStatus = paymentStatus;
        this.paymentDateTime = paymentDateTime;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public Integer getAuctionId() {
        return auctionId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public BigDecimal getFinalSalePrice() {
        return finalSalePrice;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public BigDecimal getSellerPayout() {
        return sellerPayout;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getPaymentDateTime() {
        return paymentDateTime;
    }
}
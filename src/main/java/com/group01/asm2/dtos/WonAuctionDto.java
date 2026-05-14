package com.group01.asm2.dtos;

/**
 * @author Group 01
 */

import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WonAuctionDto {
    private Integer auctionId;
    private Integer itemId;
    private Integer sellerId;
    private Integer buyerId;
    private Integer paymentId;

    private String itemTitle;
    private String sellerUsername;
    private String categoryName;
    private String primaryImageUrl;

    private AuctionStatus auctionStatus;
    private PaymentStatus paymentStatus;

    private BigDecimal finalSalePrice;
    private BigDecimal totalAmount;
    private BigDecimal commissionAmount;
    private BigDecimal sellerPayout;

    private LocalDateTime wonDateTime;
    private LocalDateTime paymentDateTime;

    public WonAuctionDto() {
    }

    public WonAuctionDto(
        Integer auctionId,
        Integer itemId,
        Integer sellerId,
        Integer buyerId,
        Integer paymentId,
        String itemTitle,
        String sellerUsername,
        String categoryName,
        String primaryImageUrl,
        AuctionStatus auctionStatus,
        PaymentStatus paymentStatus,
        BigDecimal finalSalePrice,
        BigDecimal totalAmount,
        BigDecimal commissionAmount,
        BigDecimal sellerPayout,
        LocalDateTime wonDateTime,
        LocalDateTime paymentDateTime
    ) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.paymentId = paymentId;
        this.itemTitle = itemTitle;
        this.sellerUsername = sellerUsername;
        this.categoryName = categoryName;
        this.primaryImageUrl = primaryImageUrl;
        this.auctionStatus = auctionStatus;
        this.paymentStatus = paymentStatus;
        this.finalSalePrice = finalSalePrice;
        this.totalAmount = totalAmount;
        this.commissionAmount = commissionAmount;
        this.sellerPayout = sellerPayout;
        this.wonDateTime = wonDateTime;
        this.paymentDateTime = paymentDateTime;
    }

    public boolean hasPayment() {
        return paymentId != null;
    }

    public boolean isPaymentCompleted() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    public boolean isPaymentPending() {
        return paymentStatus == PaymentStatus.PENDING;
    }

    public boolean isPaymentFailed() {
        return paymentStatus == PaymentStatus.FAILED;
    }

    public boolean hasPrimaryImage() {
        return primaryImageUrl != null && !primaryImageUrl.isBlank();
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

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public AuctionStatus getAuctionStatus() {
        return auctionStatus;
    }

    public void setAuctionStatus(AuctionStatus auctionStatus) {
        this.auctionStatus = auctionStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getFinalSalePrice() {
        return finalSalePrice;
    }

    public void setFinalSalePrice(BigDecimal finalSalePrice) {
        this.finalSalePrice = finalSalePrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public BigDecimal getSellerPayout() {
        return sellerPayout;
    }

    public void setSellerPayout(BigDecimal sellerPayout) {
        this.sellerPayout = sellerPayout;
    }

    public LocalDateTime getWonDateTime() {
        return wonDateTime;
    }

    public void setWonDateTime(LocalDateTime wonDateTime) {
        this.wonDateTime = wonDateTime;
    }

    public LocalDateTime getPaymentDateTime() {
        return paymentDateTime;
    }

    public void setPaymentDateTime(LocalDateTime paymentDateTime) {
        this.paymentDateTime = paymentDateTime;
    }
}
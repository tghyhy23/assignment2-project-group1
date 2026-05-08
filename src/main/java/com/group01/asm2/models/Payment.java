package com.group01.asm2.models;

import com.group01.asm2.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private Integer id;
    private Integer auctionId;
    private Integer buyerId;
    private Integer sellerId;
    private BigDecimal totalAmount;
    private BigDecimal commissionAmount;
    private BigDecimal sellerPayout;
    private PaymentStatus status;
    private LocalDateTime paymentDateTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
    }

    public Payment(Integer id, Integer auctionId, Integer buyerId, Integer sellerId,
                   BigDecimal totalAmount, BigDecimal commissionAmount, BigDecimal sellerPayout,
                   PaymentStatus status, LocalDateTime paymentDateTime,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.auctionId = auctionId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.totalAmount = totalAmount;
        this.commissionAmount = commissionAmount;
        this.sellerPayout = sellerPayout;
        this.status = status;
        this.paymentDateTime = paymentDateTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
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

    public Integer getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaymentDateTime() {
        return paymentDateTime;
    }

    public void setPaymentDateTime(LocalDateTime paymentDateTime) {
        this.paymentDateTime = paymentDateTime;
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
package com.group01.asm2.dtos;

import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.enums.ItemCondition;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionFilter {
    private Boolean onlyMine;
    private Boolean includeInactive;
    private Boolean recommendedOnly;
    private Integer sellerId;
    private Integer categoryId;
    private ItemCondition condition;
    private AuctionStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private LocalDateTime endingBefore;
    private LocalDateTime endingAfter;
    private String keyword;

    public Boolean getOnlyMine() {
        return onlyMine;
    }

    public void setOnlyMine(Boolean onlyMine) {
        this.onlyMine = onlyMine;
    }

    public Boolean getIncludeInactive() {
        return includeInactive;
    }

    public void setIncludeInactive(Boolean includeInactive) {
        this.includeInactive = includeInactive;
    }

    public Boolean getRecommendedOnly() {
        return recommendedOnly;
    }

    public void setRecommendedOnly(Boolean recommendedOnly) {
        this.recommendedOnly = recommendedOnly;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public LocalDateTime getEndingBefore() {
        return endingBefore;
    }

    public void setEndingBefore(LocalDateTime endingBefore) {
        this.endingBefore = endingBefore;
    }

    public LocalDateTime getEndingAfter() {
        return endingAfter;
    }

    public void setEndingAfter(LocalDateTime endingAfter) {
        this.endingAfter = endingAfter;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
package com.group01.asm2.dtos;

import com.group01.asm2.enums.ItemCondition;
import com.group01.asm2.enums.ItemStatus;

import java.math.BigDecimal;

public class ItemFilter {
    private Boolean onlyMine;
    private Boolean includeInactive;
    private Integer sellerId;
    private Integer categoryId;
    private ItemCondition condition;
    private ItemStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
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

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
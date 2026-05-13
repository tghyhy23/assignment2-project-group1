package com.group01.asm2.dtos;

/**
 * @author Group 01
 */

import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.models.Auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionCardDto {
    private Auction auction;

    private Integer auctionId;
    private Integer itemId;
    private Integer sellerId;
    private Integer categoryId;

    private String itemTitle;
    private String itemCondition;
    private String categoryName;
    private String sellerUsername;
    private String primaryImageUrl;

    private AuctionStatus status;
    private BigDecimal startingPrice;
    private BigDecimal currentBidAmount;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private int bidCount;
    private boolean recommended;
    private boolean watching;

    public AuctionCardDto() {
    }

    public AuctionCardDto(
        Auction auction,
        Integer auctionId,
        Integer itemId,
        Integer sellerId,
        Integer categoryId,
        String itemTitle,
        String itemCondition,
        String categoryName,
        String sellerUsername,
        String primaryImageUrl,
        AuctionStatus status,
        BigDecimal startingPrice,
        BigDecimal currentBidAmount,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        int bidCount,
        boolean recommended,
        boolean watching
    ) {
        this.auction = auction;
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.itemTitle = itemTitle;
        this.itemCondition = itemCondition;
        this.categoryName = categoryName;
        this.sellerUsername = sellerUsername;
        this.primaryImageUrl = primaryImageUrl;
        this.status = status;
        this.startingPrice = startingPrice;
        this.currentBidAmount = currentBidAmount;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.bidCount = bidCount;
        this.recommended = recommended;
        this.watching = watching;
    }

    public boolean isActive() {
        return status == AuctionStatus.ACTIVE;
    }

    public boolean hasPrimaryImage() {
        return primaryImageUrl != null && !primaryImageUrl.isBlank();
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
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

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getCurrentBidAmount() {
        return currentBidAmount;
    }

    public void setCurrentBidAmount(BigDecimal currentBidAmount) {
        this.currentBidAmount = currentBidAmount;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getBidCount() {
        return bidCount;
    }

    public void setBidCount(int bidCount) {
        this.bidCount = bidCount;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public boolean isWatching() {
        return watching;
    }

    public void setWatching(boolean watching) {
        this.watching = watching;
    }
}
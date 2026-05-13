package com.group01.asm2.dtos;

/**
 * @author Group 01
 */

import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Bid;
import com.group01.asm2.models.Category;
import com.group01.asm2.models.Item;
import com.group01.asm2.models.ItemImage;
import com.group01.asm2.models.Payment;
import com.group01.asm2.models.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AuctionDetailDto {
    private Auction auction;
    private Item item;
    private Category category;
    private User seller;

    private List<ItemImage> images = new ArrayList<>();
    private List<Bid> recentBids = new ArrayList<>();

    private BigDecimal currentBidAmount;
    private Integer currentHighestBidId;
    private Integer currentHighestBidderId;
    private String currentHighestBidderUsername;

    private int bidCount;

    private boolean watching;
    private boolean owner;
    private boolean canBid;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canModerate;
    private boolean canProcessAuction;

    private Payment payment;

    public AuctionDetailDto() {
    }

    public AuctionDetailDto(
        Auction auction,
        Item item,
        Category category,
        User seller,
        List<ItemImage> images,
        List<Bid> recentBids,
        BigDecimal currentBidAmount,
        Integer currentHighestBidId,
        Integer currentHighestBidderId,
        String currentHighestBidderUsername,
        int bidCount,
        boolean watching,
        boolean owner,
        boolean canBid,
        boolean canEdit,
        boolean canDelete,
        boolean canModerate,
        boolean canProcessAuction,
        Payment payment
    ) {
        this.auction = auction;
        this.item = item;
        this.category = category;
        this.seller = seller;
        this.images = images != null ? images : new ArrayList<>();
        this.recentBids = recentBids != null ? recentBids : new ArrayList<>();
        this.currentBidAmount = currentBidAmount;
        this.currentHighestBidId = currentHighestBidId;
        this.currentHighestBidderId = currentHighestBidderId;
        this.currentHighestBidderUsername = currentHighestBidderUsername;
        this.bidCount = bidCount;
        this.watching = watching;
        this.owner = owner;
        this.canBid = canBid;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canModerate = canModerate;
        this.canProcessAuction = canProcessAuction;
        this.payment = payment;
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    public ItemImage getPrimaryImage() {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
            .filter(image -> image.getDisplayOrder() != null && image.getDisplayOrder() == 0)
            .findFirst()
            .orElse(images.get(0));
    }

    public String getPrimaryImageUrl() {
        ItemImage primaryImage = getPrimaryImage();
        return primaryImage != null ? primaryImage.getImageUrl() : null;
    }

    public boolean hasRecentBids() {
        return recentBids != null && !recentBids.isEmpty();
    }

    public boolean hasPayment() {
        return payment != null;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public List<ItemImage> getImages() {
        return images;
    }

    public void setImages(List<ItemImage> images) {
        this.images = images != null ? images : new ArrayList<>();
    }

    public List<Bid> getRecentBids() {
        return recentBids;
    }

    public void setRecentBids(List<Bid> recentBids) {
        this.recentBids = recentBids != null ? recentBids : new ArrayList<>();
    }

    public BigDecimal getCurrentBidAmount() {
        return currentBidAmount;
    }

    public void setCurrentBidAmount(BigDecimal currentBidAmount) {
        this.currentBidAmount = currentBidAmount;
    }

    public Integer getCurrentHighestBidId() {
        return currentHighestBidId;
    }

    public void setCurrentHighestBidId(Integer currentHighestBidId) {
        this.currentHighestBidId = currentHighestBidId;
    }

    public Integer getCurrentHighestBidderId() {
        return currentHighestBidderId;
    }

    public void setCurrentHighestBidderId(Integer currentHighestBidderId) {
        this.currentHighestBidderId = currentHighestBidderId;
    }

    public String getCurrentHighestBidderUsername() {
        return currentHighestBidderUsername;
    }

    public void setCurrentHighestBidderUsername(String currentHighestBidderUsername) {
        this.currentHighestBidderUsername = currentHighestBidderUsername;
    }

    public int getBidCount() {
        return bidCount;
    }

    public void setBidCount(int bidCount) {
        this.bidCount = bidCount;
    }

    public boolean isWatching() {
        return watching;
    }

    public void setWatching(boolean watching) {
        this.watching = watching;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean canBid() {
        return canBid;
    }

    public void setCanBid(boolean canBid) {
        this.canBid = canBid;
    }

    public boolean canEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean canDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean canModerate() {
        return canModerate;
    }

    public void setCanModerate(boolean canModerate) {
        this.canModerate = canModerate;
    }

    public boolean canProcessAuction() {
        return canProcessAuction;
    }

    public void setCanProcessAuction(boolean canProcessAuction) {
        this.canProcessAuction = canProcessAuction;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
package com.group01.asm2.dtos;

import com.group01.asm2.models.User;

public class UserProfileViewDto {
    private User user;

    private boolean owner;
    private boolean sellerProfile;
    private boolean canEditProfile;
    private boolean canViewPrivateDetails;
    private boolean canViewWallet;
    private boolean canRequestTopUp;
    private boolean canViewActivityLog;
    private boolean canViewSellerStatistics;
    private boolean canViewListings;

    public UserProfileViewDto() {
    }

    public UserProfileViewDto(
        User user,
        boolean owner,
        boolean sellerProfile,
        boolean canEditProfile,
        boolean canViewPrivateDetails,
        boolean canViewWallet,
        boolean canRequestTopUp,
        boolean canViewActivityLog,
        boolean canViewSellerStatistics,
        boolean canViewListings
    ) {
        this.user = user;
        this.owner = owner;
        this.sellerProfile = sellerProfile;
        this.canEditProfile = canEditProfile;
        this.canViewPrivateDetails = canViewPrivateDetails;
        this.canViewWallet = canViewWallet;
        this.canRequestTopUp = canRequestTopUp;
        this.canViewActivityLog = canViewActivityLog;
        this.canViewSellerStatistics = canViewSellerStatistics;
        this.canViewListings = canViewListings;
    }

    public User getUser() {
        return user;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isSellerProfile() {
        return sellerProfile;
    }

    public boolean canEditProfile() {
        return canEditProfile;
    }

    public boolean canViewPrivateDetails() {
        return canViewPrivateDetails;
    }

    public boolean canViewWallet() {
        return canViewWallet;
    }

    public boolean canRequestTopUp() {
        return canRequestTopUp;
    }

    public boolean canViewActivityLog() {
        return canViewActivityLog;
    }

    public boolean canViewSellerStatistics() {
        return canViewSellerStatistics;
    }

    public boolean canViewListings() {
        return canViewListings;
    }
}
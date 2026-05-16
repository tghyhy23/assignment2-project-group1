package com.group01.asm2.dtos;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Group 01
 */
public class UserProfileStatisticsDto {
    private BigDecimal balance;
    private int totalListings;
    private int activeListings;
    private int soldListings;
    private double rating;

    private int itemsSold;
    private BigDecimal totalRevenue;
    private BigDecimal commissionFees;
    private double soldRatio;

    private Map<Integer, BigDecimal> averageSalePriceByCategory;
    private Map<String, Integer> totalListingsByMonth;
    private Map<String, Integer> soldListingsByMonth;
    private Map<String, Integer> unsoldListingsByMonth;

    public UserProfileStatisticsDto() {
        this.balance = BigDecimal.ZERO;
        this.totalRevenue = BigDecimal.ZERO;
        this.commissionFees = BigDecimal.ZERO;
        this.averageSalePriceByCategory = new LinkedHashMap<>();
        this.totalListingsByMonth = new LinkedHashMap<>();
        this.soldListingsByMonth = new LinkedHashMap<>();
        this.unsoldListingsByMonth = new LinkedHashMap<>();
    }

    public UserProfileStatisticsDto(
        BigDecimal balance,
        int totalListings,
        int activeListings,
        int soldListings,
        double rating
    ) {
        this();
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.totalListings = totalListings;
        this.activeListings = activeListings;
        this.soldListings = soldListings;
        this.rating = rating;
    }

    public static UserProfileStatisticsDto empty() {
        return new UserProfileStatisticsDto();
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public int getTotalListings() {
        return totalListings;
    }

    public int getActiveListings() {
        return activeListings;
    }

    public int getSoldListings() {
        return soldListings;
    }

    public double getRating() {
        return rating;
    }

    public int getItemsSold() {
        return itemsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getCommissionFees() {
        return commissionFees;
    }

    public double getSoldRatio() {
        return soldRatio;
    }

    public Map<Integer, BigDecimal> getAverageSalePriceByCategory() {
        return averageSalePriceByCategory;
    }

    public Map<String, Integer> getTotalListingsByMonth() {
        return totalListingsByMonth;
    }

    public Map<String, Integer> getSoldListingsByMonth() {
        return soldListingsByMonth;
    }

    public Map<String, Integer> getUnsoldListingsByMonth() {
        return unsoldListingsByMonth;
    }

    public void setItemsSold(int itemsSold) {
        this.itemsSold = itemsSold;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public void setCommissionFees(BigDecimal commissionFees) {
        this.commissionFees = commissionFees != null ? commissionFees : BigDecimal.ZERO;
    }

    public void setSoldRatio(double soldRatio) {
        this.soldRatio = soldRatio;
    }

    public void addAverageSalePriceByCategory(Integer categoryId, BigDecimal averageSalePrice) {
        if (categoryId == null) {
            return;
        }

        averageSalePriceByCategory.put(
            categoryId,
            averageSalePrice != null ? averageSalePrice : BigDecimal.ZERO
        );
    }

    public void addListingTrend(
        String month,
        int totalListings,
        int soldListings,
        int unsoldListings
    ) {
        if (month == null || month.trim().isEmpty()) {
            return;
        }

        String cleanMonth = month.trim();

        totalListingsByMonth.put(cleanMonth, totalListings);
        soldListingsByMonth.put(cleanMonth, soldListings);
        unsoldListingsByMonth.put(cleanMonth, unsoldListings);
    }

    public void hideBalance() {
        this.balance = BigDecimal.ZERO;
    }

    public void hideListingStats() {
        this.totalListings = 0;
        this.activeListings = 0;
        this.soldListings = 0;
    }

    public void clearSellerStatistics() {
        this.itemsSold = 0;
        this.totalRevenue = BigDecimal.ZERO;
        this.commissionFees = BigDecimal.ZERO;
        this.soldRatio = 0.0;
        this.averageSalePriceByCategory.clear();
        this.totalListingsByMonth.clear();
        this.soldListingsByMonth.clear();
        this.unsoldListingsByMonth.clear();
    }

    public void mergeSellerStatisticsFrom(UserProfileStatisticsDto sellerStatistics) {
        if (sellerStatistics == null) {
            clearSellerStatistics();
            return;
        }

        this.itemsSold = sellerStatistics.getItemsSold();
        this.totalRevenue = sellerStatistics.getTotalRevenue();
        this.commissionFees = sellerStatistics.getCommissionFees();
        this.soldRatio = sellerStatistics.getSoldRatio();

        this.averageSalePriceByCategory.clear();
        this.averageSalePriceByCategory.putAll(sellerStatistics.getAverageSalePriceByCategory());

        this.totalListingsByMonth.clear();
        this.totalListingsByMonth.putAll(sellerStatistics.getTotalListingsByMonth());

        this.soldListingsByMonth.clear();
        this.soldListingsByMonth.putAll(sellerStatistics.getSoldListingsByMonth());

        this.unsoldListingsByMonth.clear();
        this.unsoldListingsByMonth.putAll(sellerStatistics.getUnsoldListingsByMonth());
    }
}
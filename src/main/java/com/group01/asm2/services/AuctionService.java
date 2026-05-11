package com.group01.asm2.services;

import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.models.Auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuctionService {

    // Giả lập Database lưu trữ Auction
    private static final List<Auction> auctionsDb = new ArrayList<>();

    static {
        // Auction 1 (ĐANG DIỄN RA & NỔI BẬT) -> Cho trang Explore
        auctionsDb.add(new Auction(1001, 1, AuctionStatus.ACTIVE, 3004, null, new BigDecimal("185000000.00"),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                true)); // isRecommended = true

        // Auction 2 (ĐÃ KẾT THÚC & KHÔNG NỔI BẬT) -> Cho trang Lịch sử
        auctionsDb.add(new Auction(1002, 2, AuctionStatus.SOLD, 3005, 99, new BigDecimal("50000000.00"),
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(10), LocalDateTime.now(),
                false));

        // Auction 3 (ĐANG DIỄN RA & NỔI BẬT) -> Cho trang Explore
        auctionsDb.add(new Auction(1003, 3, AuctionStatus.ACTIVE, 3006, null, new BigDecimal("1250000000.00"),
                LocalDateTime.now().minusDays(2), LocalDateTime.now().plusHours(5),
                LocalDateTime.now().minusDays(2), LocalDateTime.now(),
                true)); // isRecommended = true
    }

    /** 1. LẤY TẤT CẢ AUCTION */
    public static List<Auction> getAll() {
        return new ArrayList<>(auctionsDb);
    }

    /** 2. TÌM AUCTION THEO ID (Phục vụ cho trang Detail) */
    public static Auction getAuctionById(Integer id) {
        if (id == null) return null;
        return auctionsDb.stream()
                .filter(auction -> auction.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /** 3. LẤY DANH SÁCH AUCTION NỔI BẬT (Phục vụ cho trang Explore) */
    public static List<Auction> getRecommendedAuctions() {
        return auctionsDb.stream()
                .filter(Auction::isRecommended)
                .filter(Auction::isActive)
                .collect(Collectors.toList());
    }

    public static Auction updateAuction(Integer id, Auction updatedAuction) {
        if (id == null || updatedAuction == null) return null;

        Auction existingAuction = getAuctionById(id);
        if (existingAuction == null) return null;

        existingAuction.setItemId(updatedAuction.getItemId());
        existingAuction.setStatus(updatedAuction.getStatus());
        existingAuction.setCurrentHighestBidId(updatedAuction.getCurrentHighestBidId());
        existingAuction.setWinnerId(updatedAuction.getWinnerId());
        existingAuction.setFinalSalePrice(updatedAuction.getFinalSalePrice());
        existingAuction.setStartDateTime(updatedAuction.getStartDateTime());
        existingAuction.setEndDateTime(updatedAuction.getEndDateTime());
        existingAuction.setRecommended(updatedAuction.isRecommended());
        existingAuction.setUpdatedAt(LocalDateTime.now());

        return existingAuction;
    }

    public static boolean deleteAuction(Integer id) {
        if (id == null) return false;

        return auctionsDb.removeIf(auction -> auction.getId().equals(id));
    }
}
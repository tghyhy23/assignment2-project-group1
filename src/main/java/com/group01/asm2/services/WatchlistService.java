package com.group01.asm2.services;

/**
 * @author Group 01
 */

import com.group01.asm2.dtos.AuctionCardDto;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.AuctionWatchlist;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.AuctionWatchlistRepository;

import java.util.List;

public class WatchlistService extends BaseService {

    private final AuctionWatchlistRepository auctionWatchlistRepository = new AuctionWatchlistRepository();

    public AuctionWatchlist watchAuction(Integer auctionId) {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Check user type
        requireRegisteredUser(currentUser);

        // 3. Validate auction ID
        validateAuctionId(auctionId);

        // 4. Check auction is active
        if (!auctionWatchlistRepository.existsActiveAuctionById(auctionId)) {
            throw AppException.validation("Only active auctions can be added to your watchlist.");
        }

        // 5. Avoid duplicate watchlist record
        AuctionWatchlist existingWatchlist = auctionWatchlistRepository
            .readAuctionWatchlistByUserIdAndAuctionId(currentUser.getId(), auctionId);

        if (existingWatchlist != null) {
            return existingWatchlist;
        }

        // 6. Create watchlist record
        AuctionWatchlist watchlist = new AuctionWatchlist(
            null,
            currentUser.getId(),
            auctionId,
            null
        );

        return auctionWatchlistRepository.createAuctionWatchlist(watchlist);
    }

    public void unwatchAuction(Integer auctionId) {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Check user type
        requireRegisteredUser(currentUser);

        // 3. Validate auction ID
        validateAuctionId(auctionId);

        // 4. Check existing watchlist record
        AuctionWatchlist existingWatchlist = auctionWatchlistRepository
            .readAuctionWatchlistByUserIdAndAuctionId(currentUser.getId(), auctionId);

        if (existingWatchlist == null) {
            throw AppException.notFound("This auction is not in your watchlist.");
        }

        // 5. Delete watchlist record
        auctionWatchlistRepository.deleteAuctionWatchlistByUserIdAndAuctionId(
            currentUser.getId(),
            auctionId
        );
    }

    public List<AuctionCardDto> readMyWatchlist() {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Check user type
        requireRegisteredUser(currentUser);

        // 3. Read watchlist auction cards
        return auctionWatchlistRepository.readWatchlistAuctionCardsByUserId(currentUser.getId());
    }

    public boolean isWatchingAuction(Integer auctionId) {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Check user type
        requireRegisteredUser(currentUser);

        // 3. Validate auction ID
        validateAuctionId(auctionId);

        // 4. Check watchlist existence
        return auctionWatchlistRepository.existsByUserIdAndAuctionId(
            currentUser.getId(),
            auctionId
        );
    }

    private void requireRegisteredUser(Person currentUser) {
        if (currentUser == null || !currentUser.isRegisteredUser()) {
            throw AppException.authorization("Only registered users can use the watchlist.");
        }
    }

    private void validateAuctionId(Integer auctionId) {
        if (auctionId == null || auctionId <= 0) {
            throw AppException.validation("Invalid auction ID.");
        }
    }
}
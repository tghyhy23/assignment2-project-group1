package com.group01.asm2.services;

import com.group01.asm2.constants.ActivityTarget;
import com.group01.asm2.dtos.AuctionCardDto;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.AuctionWatchlist;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.AuctionWatchlistRepository;

import java.util.List;

/**
 * @author Group 01
 */
public class WatchlistService extends BaseService {

    private final AuctionWatchlistRepository auctionWatchlistRepository;
    private final ActivityLogService activityLogService;

    public WatchlistService() {
        this(new AuctionWatchlistRepository(), new ActivityLogService());
    }

    public WatchlistService(AuctionWatchlistRepository auctionWatchlistRepository) {
        this(auctionWatchlistRepository, new ActivityLogService());
    }

    public WatchlistService(AuctionWatchlistRepository auctionWatchlistRepository,
                            ActivityLogService activityLogService) {
        this.auctionWatchlistRepository = auctionWatchlistRepository;
        this.activityLogService = activityLogService;
    }

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

        AuctionWatchlist createdWatchlist = auctionWatchlistRepository.createAuctionWatchlist(watchlist);

        // 7. Create activity log
        activityLogService.createActivityLog(
            ActivityActionType.ADD_TO_WATCHLIST,
            ActivityTarget.WATCHLIST,
            createdWatchlist.getId(),
            "Added auction ID " + auctionId + " to watchlist."
        );

        return createdWatchlist;
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

        // 6. Create activity log
        activityLogService.createActivityLog(
            ActivityActionType.REMOVE_FROM_WATCHLIST,
            ActivityTarget.WATCHLIST,
            existingWatchlist.getId(),
            "Removed auction ID " + auctionId + " from watchlist."
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
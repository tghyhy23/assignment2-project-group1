package com.group01.asm2.services;

/**
 * @author Group 01
 */

import com.group01.asm2.configs.DatabaseConfig;
import com.group01.asm2.dtos.BidHistoryDto;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Bid;
import com.group01.asm2.models.Item;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.BidRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class BidService extends BaseService {

    private static final BigDecimal MIN_BID_INCREMENT = new BigDecimal("1.00");

    private final BidRepository bidRepository = new BidRepository();

    public Bid placeBid(Integer auctionId, BigDecimal amount) {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Check user type
        requireRegisteredUser(currentUser);

        // 3. Validate input
        validateAuctionId(auctionId);
        validateBidAmount(amount);

        try (Connection conn = DatabaseConfig.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // 4. Lock auction row to avoid race condition
                Auction auction = bidRepository.readAuctionByIdForUpdate(conn, auctionId);

                if (auction == null) {
                    throw AppException.notFound("Auction not found.");
                }

                // 5. Check auction status
                if (!auction.isActive()) {
                    throw AppException.validation("You can only bid on active auctions.");
                }

                if (auction.isDue(LocalDateTime.now())) {
                    throw AppException.validation("This auction has already ended.");
                }

                // 6. Read item
                Item item = bidRepository.readItemById(conn, auction.getItemId());

                if (item == null) {
                    throw AppException.notFound("Auction item not found.");
                }

                if (!item.isActive()) {
                    throw AppException.validation("This item is not active.");
                }

                // 7. Prevent bidding on own item
                if (item.isOwnedBy(currentUser.getId())) {
                    throw AppException.authorization("You cannot bid on your own item.");
                }

                // 8. Validate bid against current highest bid / starting price
                Bid highestBid = bidRepository.readHighestBidByAuctionId(conn, auctionId);
                validateBidAgainstCurrentPrice(amount, item, highestBid);

                // 9. Create immutable bid record
                Bid bid = new Bid(
                    null,
                    auctionId,
                    item.getId(),
                    currentUser.getId(),
                    amount,
                    LocalDateTime.now()
                );

                Bid createdBid = bidRepository.createBid(conn, bid);

                if (createdBid == null) {
                    throw AppException.database("Could not create bid.");
                }

                // 10. Update auction current highest bid
                bidRepository.updateAuctionCurrentHighestBidId(
                    conn,
                    auctionId,
                    createdBid.getId()
                );

                conn.commit();

                return createdBid;

            } catch (Exception e) {
                conn.rollback();

                if (e instanceof AppException appException) {
                    throw appException;
                }

                throw AppException.database("Could not place bid. Reason: " + e.getMessage());

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw AppException.database("Could not place bid. Reason: " + e.getMessage());
        }
    }

    public Bid readBid(Integer bidId) {
        // 1. Validate ID
        validateBidId(bidId);

        // 2. Read bid
        Bid bid = bidRepository.readBidById(bidId);

        if (bid == null) {
            throw AppException.notFound("Bid not found.");
        }

        return bid;
    }

    public List<Bid> readAuctionBids(Integer auctionId) {
        // 1. Validate auction ID
        validateAuctionId(auctionId);

        // 2. Read bids by auction
        return bidRepository.readBidsByAuctionId(auctionId);
    }

    public List<BidHistoryDto> readMyBidHistory() {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Check user type
        requireRegisteredUser(currentUser);

        // 3. Read bid history for current user
        return bidRepository.readBidHistoryByBidderId(currentUser.getId());
    }

    public List<Bid> readMyBids() {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Check user type
        requireRegisteredUser(currentUser);

        // 3. Read raw bids
        return bidRepository.readBidsByBidderId(currentUser.getId());
    }

    public void deleteBid(Integer bidId) {
        // 1. Check admin
        Person currentUser = getCurrentUserOrThrow();

        if (!currentUser.isSystemAdministrator()) {
            throw AppException.authorization("Only the system administrator can delete bid records.");
        }

        // 2. Validate bid ID
        validateBidId(bidId);

        // 3. Check bid exists
        Bid existingBid = bidRepository.readBidById(bidId);

        if (existingBid == null) {
            throw AppException.notFound("Bid not found.");
        }

        // 4. Delete bid
        bidRepository.deleteBidById(bidId);
    }

    private void requireRegisteredUser(Person currentUser) {
        if (currentUser == null || !currentUser.isRegisteredUser()) {
            throw AppException.authorization("Only registered users can perform this bid action.");
        }
    }

    private void validateBidId(Integer bidId) {
        if (bidId == null || bidId <= 0) {
            throw AppException.validation("Invalid bid ID.");
        }
    }

    private void validateAuctionId(Integer auctionId) {
        if (auctionId == null || auctionId <= 0) {
            throw AppException.validation("Invalid auction ID.");
        }
    }

    private void validateBidAmount(BigDecimal amount) {
        if (amount == null) {
            throw AppException.validation("Bid amount is required.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.validation("Bid amount must be greater than zero.");
        }
    }

    private void validateBidAgainstCurrentPrice(BigDecimal amount, Item item, Bid highestBid) {
        if (highestBid == null) {
            if (amount.compareTo(item.getStartingPrice()) < 0) {
                throw AppException.validation(
                    "First bid must be at least the starting price: "
                        + item.getStartingPrice()
                );
            }

            return;
        }

        BigDecimal minimumAllowedBid = highestBid.getAmount().add(MIN_BID_INCREMENT);

        if (amount.compareTo(minimumAllowedBid) < 0) {
            throw AppException.validation(
                "Bid must be at least " + minimumAllowedBid
                    + " to exceed the current highest bid."
            );
        }
    }
}
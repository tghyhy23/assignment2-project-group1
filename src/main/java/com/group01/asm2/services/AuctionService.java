package com.group01.asm2.services;

import com.group01.asm2.configs.DatabaseConfig;
import com.group01.asm2.core.SessionManager;
import com.group01.asm2.dtos.AuctionDetailDto;
import com.group01.asm2.dtos.AuctionFilter;
import com.group01.asm2.dtos.WonAuctionDto;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.enums.ItemStatus;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.constants.ActivityTarget;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Item;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.AuctionRepository;
import com.group01.asm2.repositories.ItemRepository;
import com.group01.asm2.security.Permission;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AuctionService extends BaseService {
    private static final int DEFAULT_AUCTION_DAYS = 7;

    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;
    private final ActivityLogService activityLogService;

    public AuctionService() {
        this(
            new AuctionRepository(),
            new ItemRepository(),
            new ActivityLogService()
        );
    }

    public AuctionService(AuctionRepository auctionRepository,
                          ItemRepository itemRepository,
                          ActivityLogService activityLogService) {
        this.auctionRepository = auctionRepository;
        this.itemRepository = itemRepository;
        this.activityLogService = activityLogService;
    }

    public Auction createAuction(Integer itemId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // 1. Check current user and authorization
        getCurrentUserOrThrow();
        requireCurrentUser(Permission.CREATE_AUCTION);

        // 2. Validate item and time input
        Integer validItemId = validateId(itemId, "Item ID");

        Item item = itemRepository.readItemById(validItemId);
        if (item == null) {
            throw AppException.notFound("Item not found.");
        }

        if (auctionRepository.existsAuctionForItem(validItemId)) {
            throw AppException.conflict("Auction already exists for this item.");
        }

        LocalDateTime start = startDateTime == null ? LocalDateTime.now() : startDateTime;
        LocalDateTime end = endDateTime == null ? start.plusDays(DEFAULT_AUCTION_DAYS) : endDateTime;

        validateAuctionTime(start, end);

        // 3. Build auction
        Auction auction = new Auction();
        auction.setItemId(validItemId);
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setCurrentHighestBidId(null);
        auction.setWinnerId(null);
        auction.setFinalSalePrice(null);
        auction.setStartDateTime(start);
        auction.setEndDateTime(end);
        auction.setRecommended(false);

        // 4. Save auction
        Auction createdAuction = auctionRepository.createAuction(auction);

        // 5. Record activity log
        activityLogService.createActivityLog(
            ActivityActionType.CREATE_AUCTION,
            "Auction",
            createdAuction.getId(),
            "Created auction for item ID " + validItemId
        );

        // 6. Return created auction
        return createdAuction;
    }

    public AuctionDetailDto readAuction(Integer auctionId) {
        // 1. Validate auction ID
        Integer validAuctionId = validateId(auctionId, "Auction ID");

        // 2. Read full detail DTO from repository
        AuctionDetailDto detail = auctionRepository.readAuction(validAuctionId);

        if (detail == null || detail.getAuction() == null) {
            throw AppException.notFound("Auction not found.");
        }

        if (detail.getItem() == null) {
            throw AppException.notFound("Item for auction not found.");
        }

        // 3. Check visibility using existing auction visibility rule
        Person currentUser = SessionManager.getCurrentUser();

        if (!canViewAuction(currentUser, detail.getAuction(), detail.getItem())) {
            throw AppException.notFound("Auction not available.");
        }

        // 4. Resolve current user flags
        boolean loggedIn = currentUser != null;
        boolean owner = loggedIn && detail.getItem().isOwnedBy(currentUser.getId());
        boolean admin = loggedIn && isAdmin(currentUser);

        boolean activeAndNotDue = detail.getAuction().isActive()
            && !detail.getAuction().isDue(LocalDateTime.now());

        boolean canBid = loggedIn
            && !owner
            && !admin
            && detail.getItem().isActive()
            && activeAndNotDue;

        detail.setOwner(owner);
        detail.setCanBid(canBid);
        detail.setCanEdit(loggedIn && (owner || admin));
        detail.setCanDelete(loggedIn && (owner || admin));
        detail.setCanModerate(admin);
        detail.setCanProcessAuction(admin);

        // Watchlist can be connected later through AuctionWatchlistRepository.
        detail.setWatching(false);

        return detail;
    }

    public List<Auction> readAuctions(AuctionFilter filter) {
        // 1. Normalize filter
        AuctionFilter safeFilter = filter == null ? new AuctionFilter() : filter;
        Person currentUser = SessionManager.getCurrentUser();

        // 2. Validate filter
        validateAuctionFilter(safeFilter);

        // 3. Choose broad repository query
        List<Auction> auctions;

        if (Boolean.TRUE.equals(safeFilter.getOnlyMine())) {
            Person owner = getCurrentUserOrThrow();
            auctions = auctionRepository.readAuctionsBySellerId(owner.getId());

        } else if (safeFilter.getSellerId() != null) {
            auctions = auctionRepository.readAuctionsBySellerId(safeFilter.getSellerId());

        } else if (canReadInactiveAuctions(currentUser, safeFilter)) {
            auctions = auctionRepository.readAuctions();

        } else {
            auctions = auctionRepository.readActiveAuctions();
        }

        // 4. Apply visibility and filter rules
        return auctions.stream()
            .filter(auction -> {
                Item item = itemRepository.readItemById(auction.getItemId());
                return canViewAuction(currentUser, auction, item);
            })
            .filter(auction -> {
                Item item = itemRepository.readItemById(auction.getItemId());
                return matchesAuctionFilter(auction, item, safeFilter);
            })
            .collect(Collectors.toList());
    }

    public Auction updateAuction(Integer auctionId,
                                 LocalDateTime startDateTime,
                                 LocalDateTime endDateTime,
                                 AuctionStatus status,
                                 BigDecimal reservePrice,
                                 Boolean recommended) {
        // 1. Check current user and authorization
        getCurrentUserOrThrow();
        requireCurrentUser(Permission.UPDATE_AUCTION);

        // 2. Validate auction ID
        Integer validAuctionId = validateId(auctionId, "Auction ID");

        // 3. Read existing auction detail
        AuctionDetailDto existingDetail = auctionRepository.readAuction(validAuctionId);
        if (existingDetail == null || existingDetail.getAuction() == null) {
            throw AppException.notFound("Auction not found.");
        }

        Auction existingAuction = existingDetail.getAuction();
        Item item = existingDetail.getItem();

        if (item == null) {
            throw AppException.notFound("Item for auction not found.");
        }

        // 4. Resolve update values
        LocalDateTime resolvedStart = startDateTime == null
            ? existingAuction.getStartDateTime()
            : startDateTime;

        LocalDateTime resolvedEnd = endDateTime == null
            ? existingAuction.getEndDateTime()
            : endDateTime;

        AuctionStatus resolvedStatus = status == null
            ? existingAuction.getStatus()
            : status;

        boolean resolvedRecommended = recommended == null
            ? existingAuction.isRecommended()
            : recommended;

        // 5. Validate update request
        validateAuctionTime(resolvedStart, resolvedEnd);
        validateStatusTransition(existingAuction, resolvedStatus);
        validateReservePrice(item, reservePrice);

        boolean hasBids = auctionRepository.hasBids(validAuctionId);
        boolean hasPayment = auctionRepository.hasPayment(validAuctionId);

        if (hasBids) {
            if (!Objects.equals(resolvedStart, existingAuction.getStartDateTime())) {
                throw AppException.conflict("Cannot change auction start time after bids exist.");
            }

            if (resolvedEnd.isBefore(existingAuction.getEndDateTime())) {
                throw AppException.conflict("Cannot shorten auction end time after bids exist.");
            }
        }

        if (hasPayment && existingAuction.getStatus() == AuctionStatus.SOLD) {
            throw AppException.conflict("Cannot update sold auction because payment already exists.");
        }

        // 6. Apply update values
        existingAuction.setStartDateTime(resolvedStart);
        existingAuction.setEndDateTime(resolvedEnd);
        existingAuction.setStatus(resolvedStatus);
        existingAuction.setRecommended(resolvedRecommended);

        if (reservePrice != null) {
            item.setReservePrice(reservePrice);
        }

        // 7. Save auction and optional item reserve price in one transaction
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Auction updatedAuction = auctionRepository.updateAuction(conn, existingAuction);

                if (updatedAuction == null) {
                    throw AppException.notFound("Auction not found.");
                }

                if (reservePrice != null) {
                    itemRepository.updateItem(conn, item);
                }

                ActivityActionType logActionType = resolvedStatus == AuctionStatus.CANCELLED
                    ? ActivityActionType.CANCEL_AUCTION
                    : ActivityActionType.UPDATE_AUCTION;

                activityLogService.createActivityLog(
                    conn,
                    logActionType,
                    ActivityTarget.AUCTION,
                    updatedAuction.getId(),
                    resolvedStatus == AuctionStatus.CANCELLED
                        ? "Cancelled auction ID " + updatedAuction.getId() + "."
                        : "Updated auction ID " + updatedAuction.getId() + "."
                );

                if (reservePrice != null) {
                    activityLogService.createActivityLog(
                        conn,
                        ActivityActionType.UPDATE_ITEM,
                        ActivityTarget.ITEM,
                        item.getId(),
                        "Updated reserve price for item ID " + item.getId() + " through auction moderation."
                    );
                }

                conn.commit();

                return updatedAuction;

            } catch (Exception exception) {
                conn.rollback();
                throw exception;
            }

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Could not update auction.");
        }
    }

    public void deleteAuction(Integer auctionId) {
        // 1. Check current user and authorization
        getCurrentUserOrThrow();
        requireCurrentUser(Permission.DELETE_AUCTION);

        // 2. Validate auction ID
        Integer validAuctionId = validateId(auctionId, "Auction ID");

        // 3. Read existing auction detail
        AuctionDetailDto existingDetail = auctionRepository.readAuction(validAuctionId);
        if (existingDetail == null || existingDetail.getAuction() == null) {
            throw AppException.notFound("Auction not found.");
        }

        Auction auction = existingDetail.getAuction();
        Item item = existingDetail.getItem();

        if (item == null) {
            throw AppException.notFound("Item for auction not found.");
        }

        // 4. Decide hard delete or moderation cancellation
        boolean hasBids = auctionRepository.hasBids(validAuctionId);
        boolean hasPayment = auctionRepository.hasPayment(validAuctionId);

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (!hasBids && !hasPayment) {
                    auctionRepository.deleteAuction(conn, auction.getId());
                    itemRepository.deleteItem(conn, item.getId());

                    activityLogService.createActivityLog(
                        conn,
                        ActivityActionType.DELETE_AUCTION,
                        ActivityTarget.AUCTION,
                        auction.getId(),
                        "Deleted auction ID " + auction.getId() + "."
                    );

                    activityLogService.createActivityLog(
                        conn,
                        ActivityActionType.MODERATE_ITEM,
                        ActivityTarget.ITEM,
                        item.getId(),
                        "Deleted associated item listing during auction deletion. Item ID: " + item.getId() + "."
                    );
                } else {
                    auction.setStatus(AuctionStatus.CANCELLED);
                    item.setStatus(ItemStatus.REMOVED);

                    auctionRepository.updateAuction(conn, auction);
                    itemRepository.updateItem(conn, item);

                    activityLogService.createActivityLog(
                        conn,
                        ActivityActionType.CANCEL_AUCTION,
                        ActivityTarget.AUCTION,
                        auction.getId(),
                        "Cancelled auction ID " + auction.getId() + " because it already has bids or payment records."
                    );

                    activityLogService.createActivityLog(
                        conn,
                        ActivityActionType.MODERATE_ITEM,
                        ActivityTarget.ITEM,
                        item.getId(),
                        "Removed associated item listing after auction cancellation. Item ID: " + item.getId() + "."
                    );
                }

                conn.commit();

            } catch (Exception exception) {
                conn.rollback();
                throw exception;
            }

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Could not delete auction.");
        }
    }

    public List<WonAuctionDto> readWonAuctionsForCurrentUser() {
        // 1. Require logged-in user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Optional authorization check
        // Use this only if READ_AUCTION exists and is assigned to registered users.
        requireCurrentUser(Permission.READ_AUCTION);

        // 3. Read won auction purchase records
        return auctionRepository.readWonAuctionsByBuyerId(currentUser.getId());
    }

    private boolean canViewAuction(Person currentUser, Auction auction, Item item) {
        if (auction == null || item == null) {
            return false;
        }

        if (currentUser != null && isAdmin(currentUser)) {
            return true;
        }

        if (currentUser != null && item.isOwnedBy(currentUser.getId())) {
            return true;
        }

        if (currentUser != null && Objects.equals(auction.getWinnerId(), currentUser.getId())) {
            return true;
        }

        if (!item.isActive()) {
            return false;
        }

        return auction.getStatus() == AuctionStatus.ACTIVE
            && auction.getEndDateTime() != null
            && auction.getEndDateTime().isAfter(LocalDateTime.now());
    }

    private boolean canReadInactiveAuctions(Person currentUser, AuctionFilter filter) {
        if (!Boolean.TRUE.equals(filter.getIncludeInactive())) {
            return false;
        }

        return currentUser != null && isAdmin(currentUser);
    }

    private boolean matchesAuctionFilter(Auction auction, Item item, AuctionFilter filter) {
        if (item == null) {
            return false;
        }

        if (filter.getStatus() != null && auction.getStatus() != filter.getStatus()) {
            return false;
        }

        if (filter.getRecommendedOnly() != null
            && filter.getRecommendedOnly()
            && !auction.isRecommended()) {
            return false;
        }

        if (filter.getCategoryId() != null && !Objects.equals(item.getCategoryId(), filter.getCategoryId())) {
            return false;
        }

        if (filter.getCondition() != null && item.getCondition() != filter.getCondition()) {
            return false;
        }

        if (filter.getMinPrice() != null && item.getStartingPrice().compareTo(filter.getMinPrice()) < 0) {
            return false;
        }

        if (filter.getMaxPrice() != null && item.getStartingPrice().compareTo(filter.getMaxPrice()) > 0) {
            return false;
        }

        if (filter.getEndingBefore() != null && auction.getEndDateTime().isAfter(filter.getEndingBefore())) {
            return false;
        }

        if (filter.getEndingAfter() != null && auction.getEndDateTime().isBefore(filter.getEndingAfter())) {
            return false;
        }

        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            String keyword = filter.getKeyword().trim().toLowerCase();

            String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase();
            String description = item.getDescription() == null ? "" : item.getDescription().toLowerCase();

            return title.contains(keyword) || description.contains(keyword);
        }

        return true;
    }

    private void validateAuctionFilter(AuctionFilter filter) {
        if (filter.getSellerId() != null) {
            validateId(filter.getSellerId(), "Seller ID");
        }

        if (filter.getCategoryId() != null) {
            validateId(filter.getCategoryId(), "Category ID");
        }

        if (filter.getMinPrice() != null && filter.getMinPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw AppException.validation("Minimum price cannot be negative.");
        }

        if (filter.getMaxPrice() != null && filter.getMaxPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw AppException.validation("Maximum price cannot be negative.");
        }

        if (filter.getMinPrice() != null && filter.getMaxPrice() != null
            && filter.getMaxPrice().compareTo(filter.getMinPrice()) < 0) {
            throw AppException.validation("Maximum price cannot be lower than minimum price.");
        }

        if (filter.getEndingBefore() != null && filter.getEndingAfter() != null
            && filter.getEndingBefore().isBefore(filter.getEndingAfter())) {
            throw AppException.validation("Ending before date cannot be earlier than ending after date.");
        }
    }

    private void validateAuctionTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null) {
            throw AppException.validation("Auction start date/time is required.");
        }

        if (endDateTime == null) {
            throw AppException.validation("Auction end date/time is required.");
        }

        if (!endDateTime.isAfter(startDateTime)) {
            throw AppException.validation("Auction end date/time must be after start date/time.");
        }
    }

    private void validateStatusTransition(Auction existingAuction, AuctionStatus newStatus) {
        if (newStatus == null) {
            throw AppException.validation("Auction status is required.");
        }

        AuctionStatus currentStatus = existingAuction.getStatus();

        if ((currentStatus == AuctionStatus.SOLD
            || currentStatus == AuctionStatus.UNSOLD
            || currentStatus == AuctionStatus.CANCELLED)
            && newStatus == AuctionStatus.ACTIVE) {
            throw AppException.conflict("Cannot reactivate a terminal auction.");
        }

        if (currentStatus == AuctionStatus.ACTIVE
            && (newStatus == AuctionStatus.SOLD || newStatus == AuctionStatus.UNSOLD)) {
            throw AppException.conflict("Use auction processing to mark an auction as sold or unsold.");
        }
    }

    private void validateReservePrice(Item item, BigDecimal reservePrice) {
        if (reservePrice == null) {
            return;
        }

        if (reservePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw AppException.validation("Reserve price cannot be negative.");
        }

        if (reservePrice.compareTo(item.getStartingPrice()) < 0) {
            throw AppException.validation("Reserve price cannot be lower than starting price.");
        }
    }

    private Integer validateId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw AppException.validation(fieldName + " must be a positive number.");
        }

        return id;
    }

    private boolean isAdmin(Person person) {
        return person.getRole() == UserRole.AUCTION_ADMINISTRATOR
            || person.getRole() == UserRole.SYSTEM_ADMINISTRATOR;
    }
}
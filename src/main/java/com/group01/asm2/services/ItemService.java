package com.group01.asm2.services;

import com.group01.asm2.configs.DatabaseConfig;
import com.group01.asm2.core.SessionManager;
import com.group01.asm2.dtos.CreatedListingResult;
import com.group01.asm2.dtos.ItemFilter;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.enums.ItemCondition;
import com.group01.asm2.enums.ItemStatus;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Category;
import com.group01.asm2.models.Item;
import com.group01.asm2.models.ItemImage;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.ItemImageRepository;
import com.group01.asm2.repositories.AuctionRepository;
import com.group01.asm2.repositories.CategoryRepository;
import com.group01.asm2.repositories.ItemRepository;
import com.group01.asm2.security.Permission;
import com.group01.asm2.utils.CloudinaryUploaderUtil;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class ItemService extends BaseService {
    private static final int MAX_TITLE_LENGTH = 120;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int DEFAULT_AUCTION_DAYS = 7;
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final int MAX_ITEM_IMAGES = 5;

    private final ItemRepository itemRepository;
    private final AuctionRepository auctionRepository;
    private final CategoryRepository categoryRepository;
    private final ActivityLogService activityLogService;
    private final ItemImageRepository itemImageRepository;

    public ItemService() {
        this(
            new ItemRepository(),
            new AuctionRepository(),
            new CategoryRepository(),
            new ItemImageRepository(),
            new ActivityLogService()
        );
    }

    public ItemService(ItemRepository itemRepository,
                       AuctionRepository auctionRepository,
                       CategoryRepository categoryRepository,
                       ItemImageRepository itemImageRepository,
                       ActivityLogService activityLogService) {
        this.itemRepository = itemRepository;
        this.auctionRepository = auctionRepository;
        this.categoryRepository = categoryRepository;
        this.itemImageRepository = itemImageRepository;
        this.activityLogService = activityLogService;
    }

    public CreatedListingResult createItem(String title,
                                           String description,
                                           Integer categoryId,
                                           BigDecimal startingPrice,
                                           BigDecimal reservePrice,
                                           ItemCondition condition,
                                           List<File> imageFiles) {
        // 1. Check current user and authorization
        Person currentUser = getCurrentUserOrThrow();
        requireCurrentUser(Permission.CREATE_ITEM);

        if (!isRegisteredUser(currentUser)) {
            throw AppException.authorization("Only registered users can create item listings.");
        }

        // 2. Normalize and validate item input
        String normalizedTitle = normalizeRequiredText(title, "Item title", MAX_TITLE_LENGTH);
        String normalizedDescription = normalizeRequiredText(description, "Item description", MAX_DESCRIPTION_LENGTH);
        Integer validCategoryId = validateId(categoryId, "Category ID");

        validatePrice(startingPrice, "Starting price", true);
        validateReservePrice(startingPrice, reservePrice);
        validateCondition(condition);

        // 3. Check category exists
        Category category = categoryRepository.readCategoryById(validCategoryId);
        if (category == null) {
            throw AppException.notFound("Category not found.");
        }

        // 4. Validate and upload item images
        List<String> imageUrls = uploadImagesIfPresent(imageFiles);
        String primaryImageUrl = imageUrls.isEmpty() ? null : imageUrls.get(0);

        // 5. Build item
        Item item = new Item();
        item.setTitle(normalizedTitle);
        item.setDescription(normalizedDescription);
        item.setCategoryId(validCategoryId);
        item.setSellerId(currentUser.getId());
        item.setStartingPrice(startingPrice);
        item.setReservePrice(reservePrice);
        item.setCondition(condition);
        item.setStatus(ItemStatus.ACTIVE);

        // 6. Create item, item images, and auction in one transaction
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Item createdItem = itemRepository.createItem(conn, item);

                List<ItemImage> itemImages = buildItemImages(createdItem.getId(), imageUrls);
                List<ItemImage> createdImages = itemImageRepository.createItemImages(conn, itemImages);
                createdItem.setImages(createdImages);

                LocalDateTime now = LocalDateTime.now();

                Auction auction = new Auction();
                auction.setItemId(createdItem.getId());
                auction.setStatus(AuctionStatus.ACTIVE);
                auction.setCurrentHighestBidId(null);
                auction.setWinnerId(null);
                auction.setFinalSalePrice(null);
                auction.setStartDateTime(now);
                auction.setEndDateTime(now.plusDays(DEFAULT_AUCTION_DAYS));
                auction.setRecommended(false);

                Auction createdAuction = auctionRepository.createAuction(conn, auction);

                conn.commit();

                // 7. Record activity logs
                activityLogService.createActivityLog(
                    ActivityActionType.CREATE_ITEM,
                    "Item",
                    createdItem.getId(),
                    "Created item listing: " + createdItem.getTitle()
                );

                activityLogService.createActivityLog(
                    ActivityActionType.CREATE_AUCTION,
                    "Auction",
                    createdAuction.getId(),
                    "Automatically created auction for item ID " + createdItem.getId()
                );

                // 8. Return created listing result
                return new CreatedListingResult(createdItem, createdAuction);

            } catch (Exception exception) {
                conn.rollback();
                throw exception;
            }

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Could not create item listing.");
        }
    }

    public Item readItem(Integer itemId) {
        // 1. Validate item ID
        Integer validItemId = validateId(itemId, "Item ID");

        // 2. Read existing item
        Item item = itemRepository.readItemById(validItemId);
        if (item == null) {
            throw AppException.notFound("Item not found.");
        }

        // 3. Check visibility
        Person currentUser = SessionManager.getCurrentUser();
        Auction auction = auctionRepository.readAuctionByItemId(validItemId);

        if (!canViewItem(currentUser, item, auction)) {
            throw AppException.notFound("Item not available.");
        }

        // 4. Attach images
        attachImages(item);

        // 5. Return item
        return item;
    }

    public List<Item> readItems(ItemFilter filter) {
        // 1. Normalize filter
        ItemFilter safeFilter = filter == null ? new ItemFilter() : filter;
        Person currentUser = SessionManager.getCurrentUser();

        // 2. Validate filter
        validateItemFilter(safeFilter);

        // 3. Choose broad repository query
        List<Item> items;

        if (Boolean.TRUE.equals(safeFilter.getOnlyMine())) {
            Person owner = getCurrentUserOrThrow();
            items = itemRepository.readItemsBySellerId(owner.getId());

        } else if (safeFilter.getSellerId() != null) {
            items = itemRepository.readItemsBySellerId(safeFilter.getSellerId());

        } else if (canReadInactiveItems(currentUser, safeFilter)) {
            items = itemRepository.readItems();

        } else {
            items = itemRepository.readActiveItems();
        }

        // 4. Apply visibility and filter rules
        List<Item> result = items.stream()
            .filter(item -> {
                Auction auction = auctionRepository.readAuctionByItemId(item.getId());
                return canViewItem(currentUser, item, auction);
            })
            .filter(item -> matchesItemFilter(item, safeFilter))
            .collect(Collectors.toList());

        // 5. Attach images
        result.forEach(this::attachImages);

        // 6. Return items
        return result;
    }

    public Item updateItem(Integer itemId,
                           String title,
                           String description,
                           Integer categoryId,
                           BigDecimal startingPrice,
                           BigDecimal reservePrice,
                           ItemCondition condition,
                           List<File> newImageFiles) {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Validate target item ID
        Integer validItemId = validateId(itemId, "Item ID");

        // 3. Read existing item and auction
        Item existingItem = itemRepository.readItemById(validItemId);
        if (existingItem == null) {
            throw AppException.notFound("Item not found.");
        }

        Auction auction = auctionRepository.readAuctionByItemId(validItemId);
        if (auction == null) {
            throw AppException.notFound("Auction for item not found.");
        }

        // 4. Check authorization
        boolean owner = existingItem.isOwnedBy(currentUser.getId());

        if (owner) {
            requireCurrentUser(Permission.UPDATE_OWN_ITEM);
        } else {
            requireCurrentUser(Permission.MODERATE_ITEM);
        }

        // 5. Check editability
        if (owner) {
            if (!auction.isActive()) {
                throw AppException.conflict("Cannot update item because the auction is no longer active.");
            }

            if (auctionRepository.hasBids(auction.getId())) {
                throw AppException.conflict("Cannot update item because the auction already has bids.");
            }
        }

        // 6. Normalize and validate input
        String normalizedTitle = normalizeRequiredText(title, "Item title", MAX_TITLE_LENGTH);
        String normalizedDescription = normalizeRequiredText(description, "Item description", MAX_DESCRIPTION_LENGTH);
        Integer validCategoryId = validateId(categoryId, "Category ID");

        validatePrice(startingPrice, "Starting price", true);
        validateReservePrice(startingPrice, reservePrice);
        validateCondition(condition);

        Category category = categoryRepository.readCategoryById(validCategoryId);
        if (category == null) {
            throw AppException.notFound("Category not found.");
        }

        // 7. Upload new images if provided
        List<String> newImageUrls = uploadImagesIfPresent(newImageFiles);
        boolean replacingImages = !newImageUrls.isEmpty();

        // 8. Apply allowed updates
        existingItem.setTitle(normalizedTitle);
        existingItem.setDescription(normalizedDescription);
        existingItem.setCategoryId(validCategoryId);
        existingItem.setStartingPrice(startingPrice);
        existingItem.setReservePrice(reservePrice);
        existingItem.setCondition(condition);

        // 9. Save item and image changes
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Item updatedItem = itemRepository.updateItem(conn, existingItem);

                if (updatedItem == null) {
                    throw AppException.notFound("Item not found.");
                }

                if (replacingImages) {
                    itemImageRepository.deleteItemImagesByItemId(conn, updatedItem.getId());

                    List<ItemImage> itemImages = buildItemImages(updatedItem.getId(), newImageUrls);
                    List<ItemImage> createdImages = itemImageRepository.createItemImages(conn, itemImages);

                    updatedItem.setImages(createdImages);
                } else {
                    updatedItem.setImages(itemImageRepository.readItemImagesByItemId(conn, updatedItem.getId()));
                }

                conn.commit();

                // 10. Record activity log
                activityLogService.createActivityLog(
                    owner ? ActivityActionType.UPDATE_ITEM : ActivityActionType.MODERATE_ITEM,
                    "Item",
                    updatedItem.getId(),
                    "Updated item: " + updatedItem.getTitle()
                );

                // 11. Return updated item
                return updatedItem;

            } catch (Exception exception) {
                conn.rollback();
                throw exception;
            }

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Could not update item.");
        }
    }

    public void deleteItem(Integer itemId) {
        // 1. Check current user and authorization
        getCurrentUserOrThrow();
        requireCurrentUser(Permission.MODERATE_ITEM);

        // 2. Validate target item ID
        Integer validItemId = validateId(itemId, "Item ID");

        // 3. Read existing item and auction
        Item item = itemRepository.readItemById(validItemId);
        if (item == null) {
            throw AppException.notFound("Item not found.");
        }

        Auction auction = auctionRepository.readAuctionByItemId(validItemId);
        if (auction == null) {
            throw AppException.notFound("Auction for item not found.");
        }

        // 4. Decide hard delete or soft remove
        boolean hasBids = auctionRepository.hasBids(auction.getId());
        boolean hasPayment = auctionRepository.hasPayment(auction.getId());

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (!hasBids && !hasPayment) {
                    auctionRepository.deleteAuction(conn, auction.getId());
                    itemRepository.deleteItem(conn, item.getId());
                } else {
                    item.setStatus(ItemStatus.REMOVED);
                    auction.setStatus(AuctionStatus.CANCELLED);

                    itemRepository.updateItem(conn, item);
                    auctionRepository.updateAuction(conn, auction);
                }

                conn.commit();

            } catch (Exception exception) {
                conn.rollback();
                throw exception;
            }

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Could not delete item.");
        }

        // 5. Record activity log
        activityLogService.createActivityLog(
            ActivityActionType.DELETE_ITEM,
            "Item",
            validItemId,
            "Deleted or removed item ID " + validItemId
        );
    }

    private boolean canViewItem(Person currentUser, Item item, Auction auction) {
        if (item == null) {
            return false;
        }

        if (currentUser != null && isAdmin(currentUser)) {
            return true;
        }

        if (currentUser != null && item.isOwnedBy(currentUser.getId())) {
            return true;
        }

        if (!item.isActive()) {
            return false;
        }

        if (auction == null) {
            return false;
        }

        return auction.getStatus() == AuctionStatus.ACTIVE
            && auction.getEndDateTime() != null
            && auction.getEndDateTime().isAfter(LocalDateTime.now());
    }

    private boolean canReadInactiveItems(Person currentUser, ItemFilter filter) {
        if (!Boolean.TRUE.equals(filter.getIncludeInactive())) {
            return false;
        }

        return currentUser != null && isAdmin(currentUser);
    }

    private boolean matchesItemFilter(Item item, ItemFilter filter) {
        if (filter.getCategoryId() != null && !Objects.equals(item.getCategoryId(), filter.getCategoryId())) {
            return false;
        }

        if (filter.getCondition() != null && item.getCondition() != filter.getCondition()) {
            return false;
        }

        if (filter.getStatus() != null && item.getStatus() != filter.getStatus()) {
            return false;
        }

        if (filter.getMinPrice() != null && item.getStartingPrice().compareTo(filter.getMinPrice()) < 0) {
            return false;
        }

        if (filter.getMaxPrice() != null && item.getStartingPrice().compareTo(filter.getMaxPrice()) > 0) {
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

    private void validateItemFilter(ItemFilter filter) {
        if (filter.getSellerId() != null) {
            validateId(filter.getSellerId(), "Seller ID");
        }

        if (filter.getCategoryId() != null) {
            validateId(filter.getCategoryId(), "Category ID");
        }

        if (filter.getMinPrice() != null) {
            validatePrice(filter.getMinPrice(), "Minimum price", false);
        }

        if (filter.getMaxPrice() != null) {
            validatePrice(filter.getMaxPrice(), "Maximum price", false);
        }

        if (filter.getMinPrice() != null && filter.getMaxPrice() != null
            && filter.getMaxPrice().compareTo(filter.getMinPrice()) < 0) {
            throw AppException.validation("Maximum price cannot be lower than minimum price.");
        }
    }

    private List<String> uploadImagesIfPresent(List<File> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return List.of();
        }

        List<File> validImageFiles = imageFiles.stream()
            .filter(Objects::nonNull)
            .toList();

        if (validImageFiles.isEmpty()) {
            return List.of();
        }

        if (validImageFiles.size() > MAX_ITEM_IMAGES) {
            throw AppException.validation("You can upload at most " + MAX_ITEM_IMAGES + " item images.");
        }

        List<String> imageUrls = new ArrayList<>();

        for (File imageFile : validImageFiles) {
            String imageUrl = uploadImageIfPresent(imageFile);
            imageUrls.add(imageUrl);
        }

        return imageUrls;
    }

    private String uploadImageIfPresent(File imageFile) {
        if (imageFile == null) {
            return null;
        }

        if (!imageFile.exists() || !imageFile.isFile()) {
            throw AppException.validation("Image file is invalid.");
        }

        if (imageFile.length() > MAX_IMAGE_SIZE_BYTES) {
            throw AppException.validation("Image file must not exceed 5MB.");
        }

        String fileName = imageFile.getName().toLowerCase();

        if (!fileName.endsWith(".jpg")
            && !fileName.endsWith(".jpeg")
            && !fileName.endsWith(".png")
            && !fileName.endsWith(".webp")) {
            throw AppException.validation("Image file must be JPG, PNG, or WEBP.");
        }

        return CloudinaryUploaderUtil.uploadImage(imageFile);
    }

    private List<ItemImage> buildItemImages(Integer itemId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }

        List<ItemImage> itemImages = new ArrayList<>();

        for (int i = 0; i < imageUrls.size(); i++) {
            ItemImage itemImage = new ItemImage();
            itemImage.setItemId(itemId);
            itemImage.setImageUrl(imageUrls.get(i));
            itemImage.setDisplayOrder(i);

            itemImages.add(itemImage);
        }

        return itemImages;
    }

    private void attachImages(Item item) {
        if (item == null || item.getId() == null) {
            return;
        }

        List<ItemImage> images = itemImageRepository.readItemImagesByItemId(item.getId());
        item.setImages(images);
    }

    private Integer validateId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw AppException.validation(fieldName + " must be a positive number.");
        }

        return id;
    }

    private String normalizeRequiredText(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw AppException.validation(fieldName + " is required.");
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            throw AppException.validation(fieldName + " must not exceed " + maxLength + " characters.");
        }

        return normalized;
    }

    private void validatePrice(BigDecimal price, String fieldName, boolean mustBeGreaterThanZero) {
        if (price == null) {
            throw AppException.validation(fieldName + " is required.");
        }

        int comparison = price.compareTo(BigDecimal.ZERO);

        if (mustBeGreaterThanZero && comparison <= 0) {
            throw AppException.validation(fieldName + " must be greater than 0.");
        }

        if (!mustBeGreaterThanZero && comparison < 0) {
            throw AppException.validation(fieldName + " cannot be negative.");
        }
    }

    private void validateReservePrice(BigDecimal startingPrice, BigDecimal reservePrice) {
        if (reservePrice == null) {
            return;
        }

        if (reservePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw AppException.validation("Reserve price cannot be negative.");
        }

        if (reservePrice.compareTo(startingPrice) < 0) {
            throw AppException.validation("Reserve price cannot be lower than starting price.");
        }
    }

    private void validateCondition(ItemCondition condition) {
        if (condition == null) {
            throw AppException.validation("Item condition is required.");
        }
    }

    private boolean isRegisteredUser(Person person) {
        return person.getRole() == UserRole.BUYER || person.getRole() == UserRole.SELLER;
    }

    private boolean isAdmin(Person person) {
        return person.getRole() == UserRole.AUCTION_ADMINISTRATOR
            || person.getRole() == UserRole.SYSTEM_ADMINISTRATOR;
    }
}
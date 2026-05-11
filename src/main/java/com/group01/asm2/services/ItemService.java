package com.group01.asm2.services;

import com.group01.asm2.models.Item;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemService {

    private static final List<Item> itemsDb = new ArrayList<>();

    static {
        itemsDb.add(new Item(1, 99, 101,
                "Đồng hồ Rolex Submariner 2020",
                "Đồng hồ Rolex nguyên bản, đầy đủ giấy tờ, hộp sổ thẻ.",
                "Like New",
                "Rolex",
                "Ho Chi Minh City",
                new java.math.BigDecimal("150000000.00"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()));

        itemsDb.add(new Item(2, 99, 102,
                "Bức tranh sơn dầu thế kỷ 19",
                "Tác phẩm nghệ thuật độc bản từ thế kỷ 19.",
                "Vintage",
                "Unknown",
                "Hanoi",
                new java.math.BigDecimal("45000000.00"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()));

        itemsDb.add(new Item(3, 99, 103,
                "Siêu xe Ford Mustang 1969 Classic",
                "Xe cơ bắp Mỹ cổ điển, động cơ V8 mạnh mẽ.",
                "Restored",
                "Ford",
                "Danang",
                new java.math.BigDecimal("800000000.00"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()));
    }

    /** GET ALL ITEMS */
    public static List<Item> getAllItems() {
        return new ArrayList<>(itemsDb);
    }

    /** GET ITEM BY ID */
    public static Item getItemById(Integer id) {
        if (id == null) return null;

        return itemsDb.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /** CREATE ITEM */
    public static Item createItem(Item newItem) {
        if (newItem == null) return null;

        Integer newId = generateNextId();

        Item item = new Item(
                newId,
                newItem.getCategoryId(),
                newItem.getSellerId(),
                newItem.getTitle(),
                newItem.getDescription(),
                newItem.getCondition(),
                newItem.getBrand(),
                newItem.getLocation(),
                newItem.getStartingPrice(),
                newItem.getReservePrice(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // UI Fields
        item.setCurrentBid(newItem.getCurrentBid());
        item.setBidCount(newItem.getBidCount());
        item.setRecommended(newItem.isRecommended());
        item.setMainBgClass(newItem.getMainBgClass());
        item.setThumbBgClasses(newItem.getThumbBgClasses());

        itemsDb.add(item);

        return item;
    }

    /** UPDATE ITEM */
    public static Item updateItem(Integer id, Item updatedItem) {
        if (id == null || updatedItem == null) return null;

        Item existingItem = getItemById(id);

        if (existingItem == null) return null;

        existingItem.setCategoryId(updatedItem.getCategoryId());
        existingItem.setSellerId(updatedItem.getSellerId());
        existingItem.setTitle(updatedItem.getTitle());
        existingItem.setDescription(updatedItem.getDescription());
        existingItem.setCondition(updatedItem.getCondition());
        existingItem.setBrand(updatedItem.getBrand());
        existingItem.setLocation(updatedItem.getLocation());
        existingItem.setStartingPrice(updatedItem.getStartingPrice());
        existingItem.setReservePrice(updatedItem.getReservePrice());

        existingItem.setCurrentBid(updatedItem.getCurrentBid());
        existingItem.setBidCount(updatedItem.getBidCount());
        existingItem.setRecommended(updatedItem.isRecommended());
        existingItem.setMainBgClass(updatedItem.getMainBgClass());
        existingItem.setThumbBgClasses(updatedItem.getThumbBgClasses());

        existingItem.setUpdatedAt(LocalDateTime.now());

        return existingItem;
    }

    /** DELETE ITEM */
    public static Item deleteItem(Integer id) {
        if (id == null) return null;

        Item existingItem = getItemById(id);

        if (existingItem == null) return null;

        itemsDb.remove(existingItem);

        return existingItem;
    }

    /** CHECK EXIST */
    public static boolean existsById(Integer id) {
        return getItemById(id) != null;
    }

    /** AUTO GENERATE ID */
    private static Integer generateNextId() {
        if (itemsDb.isEmpty()) {
            return 1;
        }

        return itemsDb.stream()
                .map(Item::getId)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
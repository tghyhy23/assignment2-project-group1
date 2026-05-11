package com.group01.asm2.services;

import com.group01.asm2.models.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemService {

    private static final List<Item> itemsDb = new ArrayList<>();

    static {
//        itemsDb.add(new Item(1, 99, 101, "Đồng hồ Rolex Submariner 2020",
//                "Đồng hồ Rolex nguyên bản, đầy đủ giấy tờ, hộp sổ thẻ.",
//                "Like New", "Rolex", "Ho Chi Minh City",
//                new BigDecimal("150000000.00"), null, LocalDateTime.now(), LocalDateTime.now()));
//
//        itemsDb.add(new Item(2, 99, 102, "Bức tranh sơn dầu thế kỷ 19",
//                "Tác phẩm nghệ thuật độc bản từ thế kỷ 19.",
//                "Vintage", "Unknown", "Hanoi",
//                new BigDecimal("45000000.00"), null, LocalDateTime.now(), LocalDateTime.now()));
//
//        itemsDb.add(new Item(3, 99, 103, "Siêu xe Ford Mustang 1969 Classic",
//                "Xe cơ bắp Mỹ cổ điển, động cơ V8 mạnh mẽ.",
//                "Restored", "Ford", "Danang",
//                new BigDecimal("800000000.00"), null, LocalDateTime.now(), LocalDateTime.now()));
    }

    /** Lấy tất cả Items: */
    public static List<Item> getAllItems() {
        return new ArrayList<>(itemsDb);
    }

    /** Tìm Item theo ID */
    public static Item getItemById(Integer id) {
        if (id == null) return null;
        return itemsDb.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
package com.group01.asm2.services;

import com.group01.asm2.models.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemService {

    /**
     * Hàm này giả lập việc truy vấn toàn bộ sản phẩm từ Database.
     * (Sau này bạn sẽ thay bằng code JDBC gọi xuống DB ở đây)
     */
    public static List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();

        // Sản phẩm 1 (Được Recommend) - Dùng class màu p1
        items.add(new Item(
                "ITM001",
                "Đồng hồ Rolex Submariner 2020",
                150000000.0,  // Giá khởi điểm
                185000000.0,  // Giá hiện tại
                12,           // Số lượt Bid
                true,         // isRecommended = true
                "p1-main",
                new String[]{"p1-t1", "p1-t2", "p1-t3", "p1-t4"}
        ));

        // Sản phẩm 2 (KHÔNG được Recommend) - Sẽ không hiện lên trang Explore
        items.add(new Item(
                "ITM002",
                "Bức tranh sơn dầu thế kỷ 19",
                50000000.0,
                50000000.0,
                0,
                false,        // isRecommended = false
                "p2-main",
                new String[]{"p2-t1", "p2-t2", "p2-t3", "p2-t4"}
        ));

        // Sản phẩm 3 (Được Recommend) - Dùng class màu p2
        items.add(new Item(
                "ITM003",
                "Siêu xe Ford Mustang 1969 Classic",
                800000000.0,
                1250000000.0,
                34,
                true,
                "p2-main",
                new String[]{"p2-t1", "p2-t2", "p2-t3", "p2-t4"}
        ));

        // Sản phẩm 4 (Được Recommend) - Dùng class màu p3
        items.add(new Item(
                "ITM004",
                "Giày Air Jordan 1 Retro High Dior",
                120000000.0,
                155000000.0,
                8,
                true,
                "p3-main",
                new String[]{"p3-t1", "p3-t2", "p3-t3", "p3-t4"}
        ));

        return items;
    }

    /**
     * Hàm dùng riêng cho trang Explore:
     * Lọc và chỉ trả về những sản phẩm có cờ isRecommended == true
     */
    public static List<Item> getRecommendedItems() {
        // Dùng Stream API của Java 8 để lọc dữ liệu cực nhanh và gọn
        return getAllItems().stream()
                .filter(Item::isRecommended)
                .collect(Collectors.toList());
    }
}
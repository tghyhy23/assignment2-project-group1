package com.group01.asm2.controllers;

import com.group01.asm2.models.Item;
import com.group01.asm2.services.ItemService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.text.DecimalFormat;
import java.util.List;

public class ExploreController {

    @FXML private StackPane mainImagePane;
    @FXML private StackPane thumb1;
    @FXML private StackPane thumb2;
    @FXML private StackPane thumb3;
    @FXML private StackPane thumb4;

    @FXML private Label featureTitle;
    @FXML private Label bidsCountLabel; // Label Bids mới
    @FXML private Label originalPrice;
    @FXML private Label finalPrice;

    @FXML private HBox paginationContainer; // HBox chứa dấu chấm

    private List<Item> recommendedItems;
    private int currentIndex = 0;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,###đ");

    @FXML
    public void initialize() {
        recommendedItems = ItemService.getRecommendedItems();

        if (recommendedItems == null || recommendedItems.isEmpty()) return;

        // 1. Khởi tạo dấu chấm theo số lượng sản phẩm
        initPagination();

        // 2. Gắn sự kiện Hover
        setupThumbnailHover(thumb1, 0);
        setupThumbnailHover(thumb2, 1);
        setupThumbnailHover(thumb3, 2);
        setupThumbnailHover(thumb4, 3);

        // 3. Load sản phẩm đầu tiên
        loadProduct(currentIndex);
    }

    // --- LOGIC TẠO DẤU CHẤM ---
    private void initPagination() {
        paginationContainer.getChildren().clear();
        for (int i = 0; i < recommendedItems.size(); i++) {
            Region dot = new Region();
            dot.getStyleClass().add("dot");

            // Tạo một biến tạm 'index' vì biến 'i' trong vòng lặp không dùng được trực tiếp trong lambda
            final int index = i;

            // Gắn sự kiện click cho từng dấu chấm
            dot.setOnMouseClicked(event -> {
                currentIndex = index; // Cập nhật vị trí hiện tại
                loadProduct(currentIndex); // Load sản phẩm tương ứng
            });

            paginationContainer.getChildren().add(dot);
        }
    }

    // --- LOGIC CẬP NHẬT MÀU DẤU CHẤM ĐANG ACTIVE ---
    private void updatePagination() {
        for (int i = 0; i < paginationContainer.getChildren().size(); i++) {
            Node dot = paginationContainer.getChildren().get(i);
            if (i == currentIndex) {
                if (!dot.getStyleClass().contains("active")) {
                    dot.getStyleClass().add("active");
                }
            } else {
                dot.getStyleClass().remove("active");
            }
        }
    }

    private void loadProduct(int index) {
        Item item = recommendedItems.get(index);

        featureTitle.setText(item.getTitle());
        bidsCountLabel.setText("🔥 " + item.getBidCount() + " Bids");

        originalPrice.setText("Khởi điểm: " + currencyFormat.format(item.getStartingPrice()));
        finalPrice.setText("Hiện tại: " + currencyFormat.format(item.getCurrentBid()));

        // Cập nhật background
        mainImagePane.getStyleClass().removeIf(c -> c.startsWith("p1-") || c.startsWith("p2-") || c.startsWith("p3-"));
        mainImagePane.getStyleClass().add(item.getMainBgClass());

        updateThumbClass(thumb1, item.getThumbBgClasses()[0]);
        updateThumbClass(thumb2, item.getThumbBgClasses()[1]);
        updateThumbClass(thumb3, item.getThumbBgClasses()[2]);
        updateThumbClass(thumb4, item.getThumbBgClasses()[3]);

        // Đổi màu dấu chấm tương ứng
        updatePagination();
    }

    private void updateThumbClass(StackPane thumb, String newClass) {
        thumb.getStyleClass().removeIf(c -> c.startsWith("p1-") || c.startsWith("p2-") || c.startsWith("p3-"));
        thumb.getStyleClass().add(newClass);
    }

    private void setupThumbnailHover(StackPane thumb, int thumbIndex) {
        thumb.setOnMouseEntered(event -> {
            Item currentItem = recommendedItems.get(currentIndex);
            mainImagePane.getStyleClass().remove(currentItem.getMainBgClass());
            mainImagePane.getStyleClass().add(currentItem.getThumbBgClasses()[thumbIndex]);
        });

        thumb.setOnMouseExited(event -> {
            Item currentItem = recommendedItems.get(currentIndex);
            mainImagePane.getStyleClass().remove(currentItem.getThumbBgClasses()[thumbIndex]);
            if (!mainImagePane.getStyleClass().contains(currentItem.getMainBgClass())) {
                mainImagePane.getStyleClass().add(currentItem.getMainBgClass());
            }
        });
    }

    @FXML
    private void handlePrevSlide() {
        if (recommendedItems == null || recommendedItems.isEmpty()) return;
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = recommendedItems.size() - 1;
        }
        loadProduct(currentIndex);
    }

    @FXML
    private void handleNextSlide() {
        if (recommendedItems == null || recommendedItems.isEmpty()) return;
        currentIndex++;
        if (currentIndex >= recommendedItems.size()) {
            currentIndex = 0;
        }
        loadProduct(currentIndex);
    }
}
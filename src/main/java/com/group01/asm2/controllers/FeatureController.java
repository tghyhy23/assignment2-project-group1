package com.group01.asm2.controllers;

import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Item;
import com.group01.asm2.services.AuctionService;
import com.group01.asm2.services.ItemService;
import com.group01.asm2.services.NavigationService;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

public class FeatureController {

    // 1. CHỈNH SỬA: featureCard trong FXML của bạn là một HBox (feature-card)
    @FXML private HBox featureCard;

    @FXML private StackPane mainImagePane;
    @FXML private StackPane thumb1;
    @FXML private StackPane thumb2;
    @FXML private StackPane thumb3;
    @FXML private StackPane thumb4;

    @FXML private Label featureTitle;
    @FXML private Label bidsCountLabel;
    @FXML private Label originalPrice;
    @FXML private Label finalPrice;

    @FXML private HBox paginationContainer;

    private List<Auction> recommendedAuctions;
    private int currentIndex = 0;

    private final DecimalFormat currencyFormat = new DecimalFormat("$#,###.00");

    @FXML
    public void initialize() {
        recommendedAuctions = AuctionService.getRecommendedAuctions();

        if (recommendedAuctions == null || recommendedAuctions.isEmpty()) return;

        initPagination();

        setupThumbnailHover(thumb1, 1);
        setupThumbnailHover(thumb2, 2);
        setupThumbnailHover(thumb3, 3);
        setupThumbnailHover(thumb4, 4);

        loadFeaturedAuction(currentIndex);

        // 2. THIẾT LẬP SỰ KIỆN CLICK CHO THẺ BỌC (feature-card)
        if (featureCard != null) {
            featureCard.setCursor(Cursor.HAND);
            featureCard.setOnMouseClicked(event -> {
                if (recommendedAuctions != null && !recommendedAuctions.isEmpty()) {
                    Auction currentSelectedAuction = recommendedAuctions.get(currentIndex);

                    // Tìm vùng chứa nội dung chính để chuyển trang
                    Pane contentArea = (Pane) featureCard.getScene().lookup("#contentArea");

                    // Sử dụng NavigationService để chuyển trang (chuẩn DRY)
                    NavigationService.goToAuctionDetails(contentArea, currentSelectedAuction);
                }
            });
        }
    }

    // --- CÁC HÀM LOGIC GIỮ NGUYÊN ---

    private void initPagination() {
        paginationContainer.getChildren().clear();
        for (int i = 0; i < recommendedAuctions.size(); i++) {
            Region dot = new Region();
            dot.getStyleClass().add("dot");

            final int index = i;
            dot.setOnMouseClicked(event -> {
                currentIndex = index;
                loadFeaturedAuction(currentIndex);
            });

            paginationContainer.getChildren().add(dot);
        }
    }

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

    private void loadFeaturedAuction(int index) {
        Auction auction = recommendedAuctions.get(index);
        Item item = ItemService.getItemById(auction.getItemId());

        if (item == null) return;

        featureTitle.setText(item.getTitle());

        int mockBidCount = (auction.getId() % 5) + 12;
        bidsCountLabel.setText("🔥 " + mockBidCount + " Bids");

        originalPrice.setText("Original Price: " + currencyFormat.format(item.getStartingPrice()));

        BigDecimal currentPrice = auction.getFinalSalePrice() != null ? auction.getFinalSalePrice() : item.getStartingPrice();
        finalPrice.setText("Current Price: " + currencyFormat.format(currentPrice));

        String mainClass = "p" + item.getId() + "-main";

        mainImagePane.getStyleClass().removeIf(c -> c.startsWith("p1-") || c.startsWith("p2-") || c.startsWith("p3-"));
        mainImagePane.getStyleClass().add(mainClass);

        updateThumbClass(thumb1, "p" + item.getId() + "-t1");
        updateThumbClass(thumb2, "p" + item.getId() + "-t2");
        updateThumbClass(thumb3, "p" + item.getId() + "-t3");
        updateThumbClass(thumb4, "p" + item.getId() + "-t4");

        updatePagination();
    }

    private void updateThumbClass(StackPane thumb, String newClass) {
        thumb.getStyleClass().removeIf(c -> c.startsWith("p1-") || c.startsWith("p2-") || c.startsWith("p3-"));
        thumb.getStyleClass().add(newClass);
    }

    private void setupThumbnailHover(StackPane thumb, int thumbSuffixIndex) {
        thumb.setOnMouseEntered(event -> {
            Auction auction = recommendedAuctions.get(currentIndex);
            String hoverClass = "p" + auction.getItemId() + "-t" + thumbSuffixIndex;
            String mainClass = "p" + auction.getItemId() + "-main";

            mainImagePane.getStyleClass().remove(mainClass);
            mainImagePane.getStyleClass().add(hoverClass);
        });

        thumb.setOnMouseExited(event -> {
            Auction auction = recommendedAuctions.get(currentIndex);
            String hoverClass = "p" + auction.getItemId() + "-t" + thumbSuffixIndex;
            String mainClass = "p" + auction.getItemId() + "-main";

            mainImagePane.getStyleClass().remove(hoverClass);
            if (!mainImagePane.getStyleClass().contains(mainClass)) {
                mainImagePane.getStyleClass().add(mainClass);
            }
        });
    }

    @FXML
    private void handlePrevSlide() {
        if (recommendedAuctions == null || recommendedAuctions.isEmpty()) return;
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = recommendedAuctions.size() - 1;
        }
        loadFeaturedAuction(currentIndex);
    }

    @FXML
    private void handleNextSlide() {
        if (recommendedAuctions == null || recommendedAuctions.isEmpty()) return;
        currentIndex++;
        if (currentIndex >= recommendedAuctions.size()) {
            currentIndex = 0;
        }
        loadFeaturedAuction(currentIndex);
    }
}
package com.group01.asm2.controllers;

import com.group01.asm2.models.Category;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CategoriesController implements Initializable {

    @FXML
    private HBox categoryItemsContainer;

    @FXML
    private HBox paginationContainer;

    private List<Category> allCategories = new ArrayList<>();

    // Đổi currentIndex thành currentPage
    private int currentPage = 0;
    private int totalPages = 0;

    private final int ITEMS_PER_PAGE = 4;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Dữ liệu giả lập (Tạo nhiều hơn để test lướt trang)
        List<Category> mockCategories = new ArrayList<>();
        mockCategories.add(new Category(1, "ALL SPORTS", "", null, null, null));
        mockCategories.add(new Category(2, "FREE TO PLAY", "", null, null, null));
        mockCategories.add(new Category(3, "ROLE-PLAYING", "", null, null, null));
        mockCategories.add(new Category(4, "STRATEGY", "", null, null, null));
        mockCategories.add(new Category(5, "SURVIVAL", "", null, null, null));
        mockCategories.add(new Category(6, "ACTION", "", null, null, null));
        mockCategories.add(new Category(7, "ADVENTURE", "", null, null, null));
        mockCategories.add(new Category(8, "PUZZLE", "", null, null, null));
        mockCategories.add(new Category(9, "RACING", "", null, null, null)); // Item thứ 9, sẽ sang trang 3

        setCategories(mockCategories);
    }

    public void setCategories(List<Category> categories) {
        this.allCategories = categories;

        // Tính tổng số trang (Ví dụ: 9 items / 4 = 2.25 -> làm tròn lên 3 trang)
        this.totalPages = (int) Math.ceil((double) allCategories.size() / ITEMS_PER_PAGE);
        this.currentPage = 0;

        renderCategories();
        renderPaginationDots();
    }

    @FXML
    private void handlePrevSlide() {
        if (totalPages <= 0) return;

        currentPage--;
        if (currentPage < 0) {
            currentPage = totalPages - 1; // Vòng về trang cuối
        }

        renderCategories();
        updateActiveDot();
    }

    @FXML
    private void handleNextSlide() {
        if (totalPages <= 0) return;

        currentPage++;
        if (currentPage >= totalPages) {
            currentPage = 0; // Vòng về trang đầu
        }

        renderCategories();
        updateActiveDot();
    }

    private void renderCategories() {
        categoryItemsContainer.getChildren().clear();

        if (allCategories.isEmpty()) return;

        // Tính vị trí bắt đầu (startIndex) của trang hiện tại
        int startIndex = currentPage * ITEMS_PER_PAGE;

        // Hiển thị các thẻ từ startIndex đến startIndex + 4 (hoặc đến hết list)
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int itemIndex = startIndex + i;

            // Nếu itemIndex vượt quá số lượng danh mục, thì dừng vòng lặp (trang cuối có thể không đủ 4 thẻ)
            if (itemIndex >= allCategories.size()) {
                break;
            }

            Category cat = allCategories.get(itemIndex);
            StackPane card = createCategoryCard(cat, i);
            categoryItemsContainer.getChildren().add(card);
        }
    }

    private StackPane createCategoryCard(Category cat, int colorIndex) {
        StackPane cardRoot = new StackPane();
        cardRoot.getStyleClass().add("category-card-root");

        Rectangle clip = new Rectangle(240, 220);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        cardRoot.setClip(clip);

        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().addAll("category-image-container", "cat-bg-" + (colorIndex % 4 + 1));

        VBox titleContainer = new VBox();
        titleContainer.getStyleClass().add("category-title-container");

        Label titleLabel = new Label(cat.getName().toUpperCase());
        titleLabel.getStyleClass().add("category-name");
        titleContainer.getChildren().add(titleLabel);

        cardRoot.getChildren().addAll(imageContainer, titleContainer);

        StackPane.setAlignment(titleContainer, Pos.CENTER);
        titleContainer.setTranslateY(30);

        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.15), imageContainer);
        TranslateTransition moveDown = new TranslateTransition(Duration.seconds(0.15), titleContainer);

        cardRoot.setOnMouseEntered(e -> {
            scaleUp.setToX(1.1);
            scaleUp.setToY(1.1);
            scaleUp.playFromStart();

            moveDown.setToY(60);
            moveDown.playFromStart();
        });

        cardRoot.setOnMouseExited(e -> {
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.playFromStart();

            moveDown.setToY(30);
            moveDown.playFromStart();
        });

        cardRoot.setOnMouseClicked(e -> {
            System.out.println("Bạn đã click vào: " + cat.getName());
        });

        return cardRoot;
    }

    private void renderPaginationDots() {
        if (paginationContainer == null) return;
        paginationContainer.getChildren().clear();

        // Số lượng Dot = Tổng số trang (totalPages)
        for (int i = 0; i < totalPages; i++) {

            javafx.scene.layout.Region dot = new javafx.scene.layout.Region();
            dot.getStyleClass().add("dot");

            final int dotPageIndex = i;
            dot.setOnMouseClicked(e -> {
                currentPage = dotPageIndex;
                renderCategories();
                updateActiveDot();
            });

            paginationContainer.getChildren().add(dot);
        }
        updateActiveDot();
    }

    private void updateActiveDot() {
        if (paginationContainer == null) return;

        for (int i = 0; i < paginationContainer.getChildren().size(); i++) {
            javafx.scene.Node dot = paginationContainer.getChildren().get(i);

            // So sánh index của Dot với currentPage
            if (i == currentPage) {
                if (!dot.getStyleClass().contains("active")) {
                    dot.getStyleClass().add("active");
                }
            } else {
                dot.getStyleClass().remove("active");
            }
        }
    }
}
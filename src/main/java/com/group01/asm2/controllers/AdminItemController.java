package com.group01.asm2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class AdminItemController {

    // ==========================================
    // FXML INJECTIONS (LIÊN KẾT VỚI GIAO DIỆN)
    // ==========================================

    // --- Bảng & Thanh công cụ ---
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, Void> actionCol;

    // --- In-app Modal (Duyệt Item) ---
    @FXML private StackPane modalOverlay;
    @FXML private Label itemInfoLabel;
    @FXML private ComboBox<String> statusUpdateCombo;

    // ==========================================
    // DATA BINDINGS (LƯU TRỮ DỮ LIỆU)
    // ==========================================
    private ObservableList<Item> itemList = FXCollections.observableArrayList();
    private Item currentEditingItem = null; // Lưu vết item đang được Admin duyệt

    @FXML
    public void initialize() {
        // 1. Setup dữ liệu cho ComboBox
        statusFilterCombo.setItems(FXCollections.observableArrayList("All", "PENDING", "APPROVED", "FLAGGED", "REMOVED"));
        statusFilterCombo.setValue("All"); // Mặc định chọn hiển thị tất cả

        statusUpdateCombo.setItems(FXCollections.observableArrayList("APPROVED", "FLAGGED", "REMOVED"));

        // 2. Setup Cột Nút bấm và Nạp dữ liệu giả
        setupActionColumn();
        loadDummyData();

        // ==========================================
        // LOGIC LỌC DỮ LIỆU (FILTER) THỜI GIAN THỰC
        // ==========================================

        // Bước A: Bọc danh sách gốc vào FilteredList
        FilteredList<Item> filteredData = new FilteredList<>(itemList, b -> true);

        // Bước B: Lắng nghe sự kiện khi người dùng gõ vào ô Search
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter(filteredData, newValue, statusFilterCombo.getValue());
        });

        // Bước C: Lắng nghe sự kiện khi người dùng chọn Filter ComboBox
        statusFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter(filteredData, searchField.getText(), newValue);
        });

        // Bước D: Đưa dữ liệu đã lọc vào SortedList để hỗ trợ click Header sắp xếp (Sort)
        SortedList<Item> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(itemsTable.comparatorProperty());

        // Bước E: Nạp bộ dữ liệu cuối cùng vào TableView
        itemsTable.setItems(sortedData);
    }

    /**
     * Hàm thực thi bộ lọc (Kết hợp cả Search Text và Status)
     */
    private void applyFilter(FilteredList<Item> filteredData, String searchText, String status) {
        filteredData.setPredicate(item -> {
            // Lọc theo Status
            boolean matchesStatus = true;
            if (status != null && !status.equals("All")) {
                matchesStatus = item.getStatus().equalsIgnoreCase(status);
            }

            // Lọc theo Search Text (Name hoặc ID)
            boolean matchesSearch = true;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String searchLower = searchText.toLowerCase();
                matchesSearch = item.getName().toLowerCase().contains(searchLower) ||
                        String.valueOf(item.getId()).contains(searchLower);
            }

            // Dòng này chỉ hiển thị nếu thỏa mãn CẢ 2 điều kiện
            return matchesStatus && matchesSearch;
        });
    }

    // ==========================================
    // ACTION HANDLERS (XỬ LÝ NÚT BẤM)
    // ==========================================

    @FXML
    private void closeModal() {
        modalOverlay.setVisible(false);
    }

    @FXML
    private void saveModeration() {
        String newStatus = statusUpdateCombo.getValue();

        if (newStatus == null) {
            showAlert("Error", "Please select a status to update.");
            return;
        }

        if (currentEditingItem != null) {
            // TODO: Kết nối với Database để cập nhật trạng thái vật phẩm tại đây

            // Cập nhật trên giao diện
            currentEditingItem.setStatus(newStatus);
            itemsTable.refresh();
            System.out.println("Item #" + currentEditingItem.getId() + " updated to " + newStatus);
        }

        closeModal();
    }

    /**
     * Khởi tạo nút "Review" vào cột Actions của bảng
     */
    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button reviewBtn = new Button("Review");
            private final HBox pane = new HBox(reviewBtn);

            {
                pane.setAlignment(Pos.CENTER);
                getStyleClass().add("action-cell");

                // Gắn CSS class cho nút
                reviewBtn.getStyleClass().add("action-btn");

                // BẤM NÚT REVIEW SẼ MỞ MODAL
                reviewBtn.setOnAction(e -> {
                    // Lấy Item tại dòng được click
                    currentEditingItem = getTableView().getItems().get(getIndex());

                    // Điền thông tin vào Modal để Admin xem
                    itemInfoLabel.setText("ID: #" + currentEditingItem.getId() + " - " + currentEditingItem.getName() + " (Seller: " + currentEditingItem.getSellerId() + ")");
                    statusUpdateCombo.setValue(currentEditingItem.getStatus());

                    // Hiện Modal mờ
                    modalOverlay.setVisible(true);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // Giấu nút nếu dòng trống
                setGraphic(empty ? null : pane);
            }
        });
    }

    /**
     * Hàm tiện ích hiển thị popup báo lỗi
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==========================================
    // MOCK DATA & MODEL (Xóa đi khi ráp DB thật)
    // ==========================================

    private void loadDummyData() {
        itemList.addAll(
                new Item(1, "Vintage Rolex Watch", "Antiques", 101, "PENDING"),
                new Item(2, "Gaming Laptop RTX 4090", "Electronics", 102, "APPROVED"),
                new Item(3, "Counterfeit Sneakers", "Fashion", 105, "FLAGGED"),
                new Item(4, "Rare Pokémon Card", "Collectibles", 110, "PENDING")
        );
    }

    public static class Item {
        private int id;
        private String name;
        private String category;
        private int sellerId;
        private String status;

        public Item(int id, String name, String category, int sellerId, String status) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.sellerId = sellerId;
            this.status = status;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public int getSellerId() { return sellerId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
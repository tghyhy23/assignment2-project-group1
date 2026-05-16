package com.group01.asm2.controllers;

import com.group01.asm2.models.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class AdminCategoriesController {

    // --- MAIN VIEW ELEMENTS ---
    @FXML private TextField searchField;
    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Void> actionCol;

    // --- MODAL ELEMENTS ---
    @FXML private StackPane modalOverlay;
    @FXML private Label modalTitle;
    @FXML private TextField nameInput;
    @FXML private TextArea descInput;
    @FXML private TextField commInput;

    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    // Biến dùng để cất giữ category đang được edit (nếu == null nghĩa là đang Add mới)
    private Category currentEditingCategory = null;

    @FXML
    public void initialize() {
        setupActionColumn();
        categoriesTable.setItems(categoryList);
        loadDummyData();
    }

    // ==========================================
    // ACTION HANDLERS
    // ==========================================
    @FXML
    private void openAddModal() {
        currentEditingCategory = null; // Đánh dấu là chế độ Add
        modalTitle.setText("Add New Category");

        // Reset trắng các form
        nameInput.clear();
        descInput.clear();
        commInput.clear();

        // Hiện Modal
        modalOverlay.setVisible(true);
    }

    @FXML
    private void closeModal() {
        modalOverlay.setVisible(false);
    }

    @FXML
    private void saveCategory() {
        // Lấy dữ liệu từ input
        String name = nameInput.getText().trim();
        String desc = descInput.getText().trim();
        String commStr = commInput.getText().trim();

        // 1. Validate cơ bản
        if (name.isEmpty()) {
            showAlert("Error", "Category Name cannot be empty.");
            return;
        }

        BigDecimal rate = BigDecimal.ZERO;
        if (!commStr.isEmpty()) {
            try {
                rate = new BigDecimal(commStr);
            } catch (NumberFormatException e) {
                showAlert("Error", "Commission Rate must be a valid number.");
                return;
            }
        }

        // 2. Lưu dữ liệu
        if (currentEditingCategory == null) {
            // Chế độ ADD: Tạo object mới
            Category newCat = new Category(
                    (int)(System.currentTimeMillis() % 10000), // ID giả lập
                    name, desc, rate, LocalDateTime.now(), LocalDateTime.now()
            );
            categoryList.add(newCat); // Cập nhật List
            System.out.println("Added: " + name);
        } else {
            // Chế độ EDIT: Cập nhật object hiện tại
            currentEditingCategory.setName(name);
            currentEditingCategory.setDescription(desc);
            currentEditingCategory.setCommissionRate(rate);
            currentEditingCategory.setUpdatedAt(LocalDateTime.now());

            categoriesTable.refresh(); // Cập nhật lại giao diện bảng
            System.out.println("Updated: " + name);
        }

        // Xử lý xong thì đóng modal
        closeModal();
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                getStyleClass().add("action-cell");

                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().addAll("action-btn", "btn-delete");

                // BẤM NÚT EDIT
                editBtn.setOnAction(e -> {
                    Category cat = getTableView().getItems().get(getIndex());
                    currentEditingCategory = cat; // Lưu lại category đang chọn

                    // Điền dữ liệu vào Modal
                    modalTitle.setText("Edit Category: " + cat.getName());
                    nameInput.setText(cat.getName());
                    descInput.setText(cat.getDescription());
                    commInput.setText(cat.getCommissionRate() != null ? cat.getCommissionRate().toString() : "");

                    // Hiện Modal
                    modalOverlay.setVisible(true);
                });

                // BẤM NÚT DELETE
                deleteBtn.setOnAction(e -> {
                    Category cat = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Category");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Are you sure you want to delete '" + cat.getName() + "'?");

                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        categoryList.remove(cat);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadDummyData() {
        categoryList.addAll(
                new Category(1, "Electronics", "Devices, gadgets...", new BigDecimal("5.0"), LocalDateTime.now(), LocalDateTime.now()),
                new Category(2, "Antiques", "Old and valuable...", new BigDecimal("8.5"), LocalDateTime.now(), LocalDateTime.now())
        );
    }
}
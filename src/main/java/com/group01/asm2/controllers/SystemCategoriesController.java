package com.group01.asm2.controllers;

import com.group01.asm2.models.Category;
import com.group01.asm2.utils.ScrollUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class SystemCategoriesController {
    @FXML private ScrollPane mainScrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Void> actionCol;
    @FXML private StackPane modalOverlay;
    @FXML private Label modalTitle;
    @FXML private TextField nameInput;
    @FXML private TextField descInput;
    @FXML private ComboBox<String> statusInput;

    private ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private Category editingCategory = null;

    @FXML
    public void initialize() {
        if (mainScrollPane != null) ScrollUtils.makeSmooth(mainScrollPane);

        statusFilterCombo.setItems(FXCollections.observableArrayList("All", "ACTIVE"));
        statusFilterCombo.setValue("All");
        statusInput.setItems(FXCollections.observableArrayList("ACTIVE"));

        // Ánh xạ thuộc tính (Category ko có status, dùng tạm String)
        ((TableColumn<Category, String>) categoriesTable.getColumns().get(3)).setCellValueFactory(data -> new SimpleStringProperty("ACTIVE"));

        setupActionColumn();
        setupFilters();
    }

    private void setupFilters() {
        FilteredList<Category> filteredData = new FilteredList<>(categoryList, b -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        SortedList<Category> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(categoriesTable.comparatorProperty());
        categoriesTable.setItems(sortedData);
    }

    private void applyFilter(FilteredList<Category> filteredData) {
        String search = searchField.getText().toLowerCase();
        filteredData.setPredicate(cat -> search.isEmpty() || cat.getName().toLowerCase().contains(search));
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);
            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().addAll("action-btn", "btn-delete");
                editBtn.setOnAction(e -> openEditModal(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> categoryList.remove(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML private void openAddModal() {
        editingCategory = null; modalTitle.setText("Add Category");
        nameInput.clear(); descInput.clear(); modalOverlay.setVisible(true);
    }

    private void openEditModal(Category cat) {
        editingCategory = cat; modalTitle.setText("Edit Category #" + cat.getId());
        nameInput.setText(cat.getName()); descInput.setText(cat.getDescription());
        modalOverlay.setVisible(true);
    }

    @FXML private void closeModal() { modalOverlay.setVisible(false); }

    @FXML private void saveCategory() {
        if (editingCategory != null) {
            editingCategory.setName(nameInput.getText());
            editingCategory.setDescription(descInput.getText());
            categoriesTable.refresh();
        }
        closeModal();
    }
}
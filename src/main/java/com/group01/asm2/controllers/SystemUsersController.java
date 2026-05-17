package com.group01.asm2.controllers;

import com.group01.asm2.models.User;
import com.group01.asm2.enums.UserRole;
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
import java.util.Optional;

public class SystemUsersController {
    @FXML private ScrollPane mainScrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Void> actionCol;
    @FXML private StackPane modalOverlay;
    @FXML private Label modalTitle;
    @FXML private TextField usernameInput;
    @FXML private TextField emailInput;
    @FXML private ComboBox<String> roleInput;
    @FXML private ComboBox<String> statusInput;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private User editingUser = null;

    @FXML
    public void initialize() {
        if (mainScrollPane != null) ScrollUtils.makeSmooth(mainScrollPane);

        roleFilterCombo.setItems(FXCollections.observableArrayList("All Roles", "BUYER", "SELLER", "AUCTION_ADMINISTRATOR", "SYSTEM_ADMINISTRATOR"));
        roleFilterCombo.setValue("All Roles");
        roleInput.setItems(FXCollections.observableArrayList("BUYER", "SELLER"));
        statusInput.setItems(FXCollections.observableArrayList("ACTIVE", "LOCKED"));

        // Ghi đè hiển thị cột Role và Status (Vì Model User không có trường status)
        ((TableColumn<User, String>) usersTable.getColumns().get(3)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().name()));
        ((TableColumn<User, String>) usersTable.getColumns().get(4)).setCellValueFactory(data -> new SimpleStringProperty("ACTIVE")); // Mặc định ACTIVE

        setupActionColumn();
        setupFilters();
    }

    private void setupFilters() {
        FilteredList<User> filteredData = new FilteredList<>(userList, b -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        roleFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedData);
    }

    private void applyFilter(FilteredList<User> filteredData) {
        String search = searchField.getText().toLowerCase();
        String role = roleFilterCombo.getValue();
        filteredData.setPredicate(user -> {
            boolean matchesSearch = search.isEmpty() || (user.getUsername() != null && user.getUsername().toLowerCase().contains(search));
            boolean matchesRole = role.equals("All Roles") || user.getRole().name().equals(role);
            return matchesSearch && matchesRole;
        });
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
                editBtn.setOnAction(e -> openEditUserModal(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteUser(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML private void openAddUserModal() {
        editingUser = null; modalTitle.setText("Add New User");
        usernameInput.clear(); emailInput.clear(); roleInput.setValue("BUYER"); statusInput.setValue("ACTIVE");
        modalOverlay.setVisible(true);
    }

    private void openEditUserModal(User user) {
        editingUser = user; modalTitle.setText("Edit User: #" + user.getId());
        usernameInput.setText(user.getUsername()); emailInput.setText(user.getEmail());
        roleInput.setValue(user.getRole().name()); modalOverlay.setVisible(true);
    }

    @FXML private void closeModal() { modalOverlay.setVisible(false); }

    @FXML private void saveUser() {
        if (editingUser != null) {
            editingUser.setUsername(usernameInput.getText());
            editingUser.setEmail(emailInput.getText());
            editingUser.setRole(UserRole.valueOf(roleInput.getValue()));
            usersTable.refresh();
        }
        closeModal();
    }

    private void deleteUser(User user) {
        if (user.getRole() == UserRole.SYSTEM_ADMINISTRATOR) return;
        new Alert(Alert.AlertType.CONFIRMATION, "Delete account?", ButtonType.YES, ButtonType.NO)
                .showAndWait().ifPresent(res -> { if (res == ButtonType.YES) userList.remove(user); });
    }
}
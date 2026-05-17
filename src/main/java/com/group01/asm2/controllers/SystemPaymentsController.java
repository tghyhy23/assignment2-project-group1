package com.group01.asm2.controllers;

import com.group01.asm2.models.Payment;
import com.group01.asm2.enums.PaymentStatus;
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

public class SystemPaymentsController {
    @FXML private ScrollPane mainScrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, Void> actionCol;
    @FXML private StackPane modalOverlay;
    @FXML private Label modalTitle;
    @FXML private ComboBox<String> statusInput;

    private ObservableList<Payment> paymentList = FXCollections.observableArrayList();
    private Payment editingPayment = null;

    @FXML
    public void initialize() {
        if (mainScrollPane != null) ScrollUtils.makeSmooth(mainScrollPane);

        statusFilterCombo.setItems(FXCollections.observableArrayList("All", "PENDING", "COMPLETED", "FAILED"));
        statusFilterCombo.setValue("All");
        statusInput.setItems(FXCollections.observableArrayList("PENDING", "COMPLETED", "FAILED"));

        // Ghi đè lấy dữ liệu từ Model Payment
        ((TableColumn<Payment, String>) paymentsTable.getColumns().get(0)).setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        ((TableColumn<Payment, String>) paymentsTable.getColumns().get(1)).setCellValueFactory(data -> new SimpleStringProperty("Buyer #" + data.getValue().getBuyerId()));
        ((TableColumn<Payment, String>) paymentsTable.getColumns().get(3)).setCellValueFactory(data -> new SimpleStringProperty("System")); // method
        ((TableColumn<Payment, String>) paymentsTable.getColumns().get(4)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentDateTime() != null ? data.getValue().getPaymentDateTime().toString() : ""));
        ((TableColumn<Payment, String>) paymentsTable.getColumns().get(5)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));

        setupActionColumn();
        setupFilters();
    }

    private void setupFilters() {
        FilteredList<Payment> filteredData = new FilteredList<>(paymentList, b -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        SortedList<Payment> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(paymentsTable.comparatorProperty());
        paymentsTable.setItems(sortedData);
    }

    private void applyFilter(FilteredList<Payment> filteredData) {
        String search = searchField.getText();
        String status = statusFilterCombo.getValue();
        filteredData.setPredicate(pay -> {
            boolean matchesSearch = search.isEmpty() || String.valueOf(pay.getId()).contains(search) || String.valueOf(pay.getBuyerId()).contains(search);
            boolean matchesStatus = status.equals("All") || pay.getStatus().name().equals(status);
            return matchesSearch && matchesStatus;
        });
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Moderate");
            private final HBox pane = new HBox(8, editBtn);
            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("action-btn");
                editBtn.setOnAction(e -> openModerateModal(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void openModerateModal(Payment pay) {
        editingPayment = pay; modalTitle.setText("Update Txn #" + pay.getId());
        statusInput.setValue(pay.getStatus().name()); modalOverlay.setVisible(true);
    }

    @FXML private void closeModal() { modalOverlay.setVisible(false); }

    @FXML private void saveStatus() {
        if (editingPayment != null) {
            editingPayment.setStatus(PaymentStatus.valueOf(statusInput.getValue()));
            paymentsTable.refresh();
        }
        closeModal();
    }
}
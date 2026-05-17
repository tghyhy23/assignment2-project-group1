package com.group01.asm2.controllers;

import com.group01.asm2.models.ActivityLog;
import com.group01.asm2.utils.ScrollUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SystemLogsController {
    @FXML private ScrollPane mainScrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private ComboBox<String> actionFilterCombo;
    @FXML private TableView<ActivityLog> logsTable;

    private ObservableList<ActivityLog> logList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (mainScrollPane != null) ScrollUtils.makeSmooth(mainScrollPane);

        roleFilterCombo.setItems(FXCollections.observableArrayList("All Roles", "BUYER", "SELLER", "SYSTEM_ADMINISTRATOR"));
        roleFilterCombo.setValue("All Roles");
        actionFilterCombo.setItems(FXCollections.observableArrayList("All Actions", "CREATE", "UPDATE", "DELETE"));
        actionFilterCombo.setValue("All Actions");

        // Ghi đè hiển thị dữ liệu từ Model ActivityLog
        ((TableColumn<ActivityLog, String>) logsTable.getColumns().get(0)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimestamp() != null ? data.getValue().getTimestamp().toString() : ""));
        ((TableColumn<ActivityLog, String>) logsTable.getColumns().get(1)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getActorRole() != null ? data.getValue().getActorRole().name() : ""));
        ((TableColumn<ActivityLog, String>) logsTable.getColumns().get(2)).setCellValueFactory(data -> new SimpleStringProperty("User #" + data.getValue().getActorId()));
        ((TableColumn<ActivityLog, String>) logsTable.getColumns().get(3)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getActionType() != null ? data.getValue().getActionType().name() : ""));
        ((TableColumn<ActivityLog, String>) logsTable.getColumns().get(4)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTargetEntity()));
        ((TableColumn<ActivityLog, String>) logsTable.getColumns().get(5)).setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        setupFilters();
    }

    private void setupFilters() {
        FilteredList<ActivityLog> filteredData = new FilteredList<>(logList, b -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        roleFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        actionFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        SortedList<ActivityLog> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(logsTable.comparatorProperty());
        logsTable.setItems(sortedData);
    }

    private void applyFilter(FilteredList<ActivityLog> filteredData) {
        String search = searchField.getText().toLowerCase();
        String role = roleFilterCombo.getValue();
        String action = actionFilterCombo.getValue();

        filteredData.setPredicate(log -> {
            boolean matchesSearch = search.isEmpty() || (log.getDescription() != null && log.getDescription().toLowerCase().contains(search));
            boolean matchesRole = role.equals("All Roles") || (log.getActorRole() != null && log.getActorRole().name().equals(role));
            boolean matchesAction = action.equals("All Actions") || (log.getActionType() != null && log.getActionType().name().equals(action));
            return matchesSearch && matchesRole && matchesAction;
        });
    }
}
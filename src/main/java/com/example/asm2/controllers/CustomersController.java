package com.example.asm2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class CustomersController {

    @FXML
    private TableView<CustomerRow> customerTable;

    @FXML
    private TableColumn<CustomerRow, String> idColumn;

    @FXML
    private TableColumn<CustomerRow, String> nameColumn;

    @FXML
    private TableColumn<CustomerRow, String> emailColumn;

    @FXML
    private TableColumn<CustomerRow, String> statusColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        customerTable.setItems(FXCollections.observableArrayList(
                new CustomerRow("C001", "Nguyen Van A", "a@gmail.com", "Active"),
                new CustomerRow("C002", "Tran Thi B", "b@gmail.com", "Inactive"),
                new CustomerRow("C003", "Le Van C", "c@gmail.com", "Active")
        ));
    }

    public static class CustomerRow {
        private final String id;
        private final String name;
        private final String email;
        private final String status;

        public CustomerRow(String id, String name, String email, String status) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getStatus() {
            return status;
        }
    }
}
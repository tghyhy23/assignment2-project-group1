package com.group01.asm2.controllers;

import com.group01.asm2.utils.ScrollUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;

public class SystemDashboardController {

    @FXML private ScrollPane mainScrollPane;

    // Các nhãn thống kê
    @FXML private Label totalUsersLabel;
    @FXML private Label activeAuctionsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label systemAlertsLabel;

    // Biểu đồ & Bảng
    @FXML private LineChart<String, Number> trafficChart;
    @FXML private TableView<SystemLog> logsTable;

    private ObservableList<SystemLog> logList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Áp dụng cuộn mượt
        if (mainScrollPane != null) {
            ScrollUtils.makeSmooth(mainScrollPane);
        }

        loadDashboardStats();
        setupTrafficChart();
        loadRecentLogs();
    }

    private void loadDashboardStats() {
        // Todo: Lấy dữ liệu thật từ Database
        totalUsersLabel.setText("1,245");
        activeAuctionsLabel.setText("342");
        totalRevenueLabel.setText("$845,230");
        systemAlertsLabel.setText("3"); // Cảnh báo đỏ
    }

    private void setupTrafficChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("System Operations");

        // Dữ liệu giả lập 7 ngày qua
        series.getData().add(new XYChart.Data<>("Mon", 120));
        series.getData().add(new XYChart.Data<>("Tue", 150));
        series.getData().add(new XYChart.Data<>("Wed", 105));
        series.getData().add(new XYChart.Data<>("Thu", 190));
        series.getData().add(new XYChart.Data<>("Fri", 210));
        series.getData().add(new XYChart.Data<>("Sat", 340));
        series.getData().add(new XYChart.Data<>("Sun", 280));

        trafficChart.getData().add(series);
    }

    private void loadRecentLogs() {
        // Todo: Lấy 5-10 logs gần nhất từ Database
        logList.addAll(
                new SystemLog("2026-05-17 20:15", "Auction Admin", "admin_lucas", "DELETE", "Item", "Removed counterfeit sneakers (ID #105)"),
                new SystemLog("2026-05-17 19:42", "User", "john_doe99", "CREATE", "Bid", "Placed bid $500 on Auction #42"),
                new SystemLog("2026-05-17 18:30", "System Admin", "super_admin", "UPDATE", "User", "Locked account 'spammer123'"),
                new SystemLog("2026-05-17 17:05", "User", "jane_smith", "PAYMENT", "Payment", "Processed payment $1200 via Credit Card"),
                new SystemLog("2026-05-17 15:22", "Auction Admin", "admin_lucas", "CREATE", "Category", "Added new category 'Digital Art'")
        );
        logsTable.setItems(logList);
    }

    // ==========================================
    // MOCK MODEL CHO BẢNG LOGS
    // ==========================================
    public static class SystemLog {
        private String timestamp;
        private String role;
        private String username;
        private String action;
        private String entity;
        private String details;

        public SystemLog(String timestamp, String role, String username, String action, String entity, String details) {
            this.timestamp = timestamp;
            this.role = role;
            this.username = username;
            this.action = action;
            this.entity = entity;
            this.details = details;
        }

        public String getTimestamp() { return timestamp; }
        public String getRole() { return role; }
        public String getUsername() { return username; }
        public String getAction() { return action; }
        public String getEntity() { return entity; }
        public String getDetails() { return details; }
    }
}
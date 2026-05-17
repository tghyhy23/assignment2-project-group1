package com.group01.asm2.controllers;

import com.group01.asm2.utils.ScrollUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class SystemReportsController {

    @FXML private ScrollPane mainScrollPane;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private Label totalRevenueLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeAuctionsLabel;

    @FXML private LineChart<String, Number> trendChart;
    @FXML private PieChart distributionChart;
    @FXML private TableView<List<String>> reportTable;

    private ObservableList<List<String>> tableData = FXCollections.observableArrayList();
    private List<String> currentHeaders;

    @FXML
    public void initialize() {
        if (mainScrollPane != null) ScrollUtils.makeSmooth(mainScrollPane);

        // Khởi tạo các loại báo cáo bao trọn hệ thống
        reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Financial Payments",
                "User Demographics",
                "Auctions & Items",
                "Bids Activity"
        ));
        reportTypeCombo.setValue("Financial Payments");

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        // Lắng nghe sự kiện đổi loại báo cáo
        reportTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> generateReport());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> generateReport());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> generateReport());

        generateReport(); // Chạy lần đầu
    }

    private void generateReport() {
        String type = reportTypeCombo.getValue();
        trendChart.getData().clear();
        reportTable.getColumns().clear();
        tableData.clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        // 1. CHIA LOGIC THEO TỪNG LOẠI BÁO CÁO
        if (type.equals("User Demographics")) {
            totalRevenueLabel.setText("N/A");
            totalUsersLabel.setText("2,450");
            activeAuctionsLabel.setText("N/A");

            series.setName("New Users");
            series.getData().add(new XYChart.Data<>("Week 1", 120));
            series.getData().add(new XYChart.Data<>("Week 2", 150));
            series.getData().add(new XYChart.Data<>("Week 3", 90));
            series.getData().add(new XYChart.Data<>("Week 4", 210));

            pieData.addAll(new PieChart.Data("Buyers", 70), new PieChart.Data("Sellers", 25), new PieChart.Data("Admins", 5));

            currentHeaders = Arrays.asList("User ID", "Username", "Role", "Join Date", "Status");
            tableData.add(Arrays.asList("101", "john_doe", "BUYER", "2026-05-01", "ACTIVE"));
            tableData.add(Arrays.asList("102", "jane_seller", "SELLER", "2026-05-12", "ACTIVE"));

        } else if (type.equals("Financial Payments")) {
            totalRevenueLabel.setText("$145,200.00");
            totalUsersLabel.setText("N/A");
            activeAuctionsLabel.setText("N/A");

            series.setName("Revenue ($)");
            series.getData().add(new XYChart.Data<>("Week 1", 25000));
            series.getData().add(new XYChart.Data<>("Week 2", 42000));
            series.getData().add(new XYChart.Data<>("Week 3", 31000));
            series.getData().add(new XYChart.Data<>("Week 4", 47200));

            pieData.addAll(new PieChart.Data("Completed", 85), new PieChart.Data("Pending", 10), new PieChart.Data("Refunded", 5));

            currentHeaders = Arrays.asList("Txn ID", "Auction ID", "Amount ($)", "Date", "Status");
            tableData.add(Arrays.asList("9001", "105", "1200.00", "2026-05-15", "COMPLETED"));
            tableData.add(Arrays.asList("9002", "112", "450.00", "2026-05-16", "PENDING"));

        } else {
            // Mặc định cho Auctions / Bids
            totalRevenueLabel.setText("N/A");
            totalUsersLabel.setText("N/A");
            activeAuctionsLabel.setText("342");

            series.setName("Activity Volume");
            series.getData().add(new XYChart.Data<>("Week 1", 400));
            series.getData().add(new XYChart.Data<>("Week 2", 600));
            series.getData().add(new XYChart.Data<>("Week 3", 450));
            series.getData().add(new XYChart.Data<>("Week 4", 800));

            pieData.addAll(new PieChart.Data("Electronics", 40), new PieChart.Data("Fashion", 35), new PieChart.Data("Antiques", 25));

            currentHeaders = Arrays.asList("ID", "Entity", "Category", "Metrics", "Status");
            tableData.add(Arrays.asList("105", "Vintage Watch", "Antiques", "45 Bids", "ACTIVE"));
            tableData.add(Arrays.asList("106", "Gaming PC", "Electronics", "12 Bids", "ENDED"));
        }

        // Cập nhật Biểu đồ
        trendChart.getData().add(series);
        distributionChart.setData(pieData);

        // 2. TẠO CỘT ĐỘNG (DYNAMIC COLUMNS) CHO BẢNG
        for (int i = 0; i < currentHeaders.size(); i++) {
            final int colIndex = i;
            TableColumn<List<String>, String> column = new TableColumn<>(currentHeaders.get(i));
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(colIndex)));
            reportTable.getColumns().add(column);
        }
        reportTable.setItems(tableData);
    }

    // ==========================================
    // TÍNH NĂNG DOWNLOAD CSV
    // ==========================================
    @FXML
    private void handleDownload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export System Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("System_Report_" + reportTypeCombo.getValue().replace(" ", "_") + ".csv");

        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Ghi Header
                writer.println(String.join(",", currentHeaders));
                // Ghi Dữ liệu
                for (List<String> row : tableData) {
                    writer.println(String.join(",", row));
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Report exported successfully to:\n" + file.getAbsolutePath());
                alert.setHeaderText("Export Success");
                alert.showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to export report: " + e.getMessage()).showAndWait();
            }
        }
    }
}
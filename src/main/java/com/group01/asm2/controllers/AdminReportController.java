package com.group01.asm2.controllers;

import com.group01.asm2.utils.ScrollUtils; // Import ScrollUtils của bạn
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AdminReportController {

    // Khai báo ScrollPane tổng để gọi ScrollUtils
    @FXML private ScrollPane mainScrollPane;

    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private Label totalRevenueLabel;
    @FXML private Label totalCommissionLabel;
    @FXML private Label successRateLabel;

    @FXML private BarChart<String, Number> revenueChart;
    @FXML private PieChart successPieChart;
    @FXML private TableView<ReportData> reportTable;

    private ObservableList<ReportData> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup Combo Box
        reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Comprehensive Summary",
                "Revenue per Category",
                "Bid Activity per Auction"
        ));
        reportTypeCombo.setValue("Comprehensive Summary");

        // Mặc định chọn 30 ngày qua
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        // ==========================================
        // LẮNG NGHE SỰ KIỆN FILTER
        // ==========================================
        reportTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> generateReport());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> generateReport());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> generateReport());

        // Lần chạy đầu tiên
        generateReport();

        if (mainScrollPane != null) {
            ScrollUtils.makeSmooth(mainScrollPane);
        }
    }

    private void generateReport() {
        String type = reportTypeCombo.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        System.out.println("Filtering Report: " + type + " | From: " + start + " To: " + end);

        tableData.clear();
        revenueChart.getData().clear();

        // 1. Dữ liệu giả định dựa trên Loại Báo Cáo
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Amount");

        if ("Revenue per Category".equals(type)) {
            // Lọc doanh thu
            totalRevenueLabel.setText("$145,000.00");
            totalCommissionLabel.setText("$10,500.00");
            successRateLabel.setText("82.0%");

            series.getData().add(new XYChart.Data<>("Electronics", 65000));
            series.getData().add(new XYChart.Data<>("Vehicles", 80000));

            tableData.addAll(
                    new ReportData(201, "Electronics", 150, new BigDecimal("65000.00"), new BigDecimal("4500.00"), "SOLD"),
                    new ReportData(202, "Vehicles", 89, new BigDecimal("80000.00"), new BigDecimal("6000.00"), "SOLD")
            );
        } else if ("Bid Activity per Auction".equals(type)) {
            // Lọc hoạt động đấu giá
            totalRevenueLabel.setText("$23,000.00");
            totalCommissionLabel.setText("$1,950.00");
            successRateLabel.setText("45.0%");

            series.getData().add(new XYChart.Data<>("Antiques", 23000));
            series.getData().add(new XYChart.Data<>("Art", 0)); // Chế độ này Art ế ẩm

            tableData.addAll(
                    new ReportData(301, "Antiques", 345, new BigDecimal("23000.00"), new BigDecimal("1950.00"), "SOLD"),
                    new ReportData(302, "Art", 0, BigDecimal.ZERO, BigDecimal.ZERO, "UNSOLD")
            );
        } else {
            // Comprehensive Summary (Mặc định)
            totalRevenueLabel.setText("$168,000.00");
            totalCommissionLabel.setText("$12,450.00");
            successRateLabel.setText("75.0%");

            series.getData().add(new XYChart.Data<>("Electronics", 45000));
            series.getData().add(new XYChart.Data<>("Antiques", 23000));
            series.getData().add(new XYChart.Data<>("Art", 15000));
            series.getData().add(new XYChart.Data<>("Vehicles", 85000));

            tableData.addAll(
                    new ReportData(101, "Electronics", 45, new BigDecimal("1200.00"), new BigDecimal("60.00"), "SOLD"),
                    new ReportData(102, "Antiques", 12, new BigDecimal("3400.00"), new BigDecimal("170.00"), "SOLD"),
                    new ReportData(103, "Art", 0, BigDecimal.ZERO, BigDecimal.ZERO, "UNSOLD"),
                    new ReportData(104, "Vehicles", 89, new BigDecimal("25000.00"), new BigDecimal("1250.00"), "SOLD")
            );
        }

        // 2. Cập nhật Biểu đồ cột & Bảng
        revenueChart.getData().add(series);
        reportTable.setItems(tableData);

        // 3. Biểu đồ tròn (Cập nhật tỷ lệ ngẫu nhiên cho sinh động)
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("SOLD", Math.random() * 80 + 20),
                new PieChart.Data("UNSOLD", Math.random() * 20),
                new PieChart.Data("CANCELLED", Math.random() * 10)
        );
        successPieChart.setData(pieChartData);
    }

    // ==========================================
    // TÍNH NĂNG DOWNLOAD BÁO CÁO (XUẤT CSV)
    // ==========================================
    @FXML
    private void handleDownload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report As CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("Auction_Report.csv");

        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Viết Tiêu đề cột
                writer.println("Auction ID,Category,Total Bids,Final Price,Commission,Status");

                // Viết Dữ liệu từng dòng
                for (ReportData data : tableData) {
                    writer.printf("%d,%s,%d,%s,%s,%s\n",
                            data.getAuctionId(),
                            data.getCategory(),
                            data.getTotalBids(),
                            data.getFinalPrice(),
                            data.getCommission(),
                            data.getStatus()
                    );
                }

                // Báo thành công
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Download Success");
                alert.setHeaderText(null);
                alert.setContentText("Report has been successfully downloaded to:\n" + file.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Download Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to save report: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    // ==========================================
    // SETUP BIỂU ĐỒ & DỮ LIỆU
    // ==========================================
    private void setupCharts() {
        // 1. Biểu đồ cột (Bar Chart)
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        series.getData().add(new XYChart.Data<>("Electronics", 45000));
        series.getData().add(new XYChart.Data<>("Antiques", 23000));
        series.getData().add(new XYChart.Data<>("Art", 15000));
        series.getData().add(new XYChart.Data<>("Vehicles", 85000));
        revenueChart.getData().add(series);

        // 2. Biểu đồ tròn (Pie Chart)
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("SOLD", 75),
                new PieChart.Data("UNSOLD", 20),
                new PieChart.Data("CANCELLED", 5)
        );
        successPieChart.setData(pieChartData);

        // Cập nhật nhãn tổng quan
        totalRevenueLabel.setText("$168,000.00");
        totalCommissionLabel.setText("$12,450.00");
        successRateLabel.setText("75.0%");
    }

    private void loadDummyData() {
        tableData.addAll(
                new ReportData(101, "Electronics", 45, new BigDecimal("1200.00"), new BigDecimal("60.00"), "SOLD"),
                new ReportData(102, "Antiques", 12, new BigDecimal("3400.00"), new BigDecimal("170.00"), "SOLD"),
                new ReportData(103, "Fashion", 0, BigDecimal.ZERO, BigDecimal.ZERO, "UNSOLD"),
                new ReportData(104, "Vehicles", 89, new BigDecimal("25000.00"), new BigDecimal("1250.00"), "SOLD")
        );
        reportTable.setItems(tableData);
    }

    // --- MOCK MODEL ---
    public static class ReportData {
        private int auctionId;
        private String category;
        private int totalBids;
        private BigDecimal finalPrice;
        private BigDecimal commission;
        private String status;

        public ReportData(int auctionId, String category, int totalBids, BigDecimal finalPrice, BigDecimal commission, String status) {
            this.auctionId = auctionId;
            this.category = category;
            this.totalBids = totalBids;
            this.finalPrice = finalPrice;
            this.commission = commission;
            this.status = status;
        }

        public int getAuctionId() { return auctionId; }
        public String getCategory() { return category; }
        public int getTotalBids() { return totalBids; }
        public BigDecimal getFinalPrice() { return finalPrice; }
        public BigDecimal getCommission() { return commission; }
        public String getStatus() { return status; }
    }
}
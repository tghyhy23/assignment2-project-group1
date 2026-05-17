package com.group01.asm2.controllers;

import com.group01.asm2.enums.AuctionStatus;
import com.group01.asm2.models.Auction;
import com.group01.asm2.models.TopUpRequest;
import com.group01.asm2.utils.ScrollUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminAuctionController {

    // --- SCROLL UTILS ---
    @FXML private ScrollPane mainScrollPane;

    // --- TAB BUTTONS ---
    @FXML private Button configTab;
    @FXML private Button dueTab;
    @FXML private Button transactionsTab;

    // --- VIEWS (Bên trong StackPane) ---
    @FXML private VBox configView;
    @FXML private VBox dueView;
    @FXML private VBox transactionsView;

    // --- BẢNG DỮ LIỆU & CỘT ACTIONS ---
    @FXML private TableView<Auction> configTable;
    @FXML private TableColumn<Auction, Void> configActionCol;

    @FXML private TableView<Auction> dueTable;
    @FXML private TableColumn<Auction, Void> dueActionCol;

    @FXML private TableView<TopUpRequest> topUpTable;
    @FXML private TableColumn<TopUpRequest, Void> topUpActionCol;

    @FXML private TableView<Auction> paymentTable;
    @FXML private TableColumn<Auction, Void> paymentActionCol;

    @FXML
    public void initialize() {
        // Mặc định hiển thị tab đầu tiên
        showConfig();

        // Setup các cột chứa nút bấm (Action Buttons)
        setupActionColumns();

        ScrollUtils.makeSmooth(mainScrollPane);

        // TODO: Load dữ liệu từ database vào các bảng
        // configTable.setItems(getMockAuctions());
    }

    // ==========================================
    // LOGIC CHUYỂN TAB
    // ==========================================
    @FXML
    private void showConfig() {
        switchTab(configTab, configView);
    }

    @FXML
    private void showDue() {
        switchTab(dueTab, dueView);
    }

    @FXML
    private void showTransactions() {
        switchTab(transactionsTab, transactionsView);
    }

    private void switchTab(Button activeBtn, javafx.scene.Node activeView) {
        // Reset CSS của tất cả các tab
        Button[] tabs = { configTab, dueTab, transactionsTab };
        for (Button tab : tabs) {
            tab.getStyleClass().remove("active-tab");
        }
        activeBtn.getStyleClass().add("active-tab");

        // Ẩn tất cả các view, chỉ hiện view được chọn
        configView.setVisible(false);
        dueView.setVisible(false);
        transactionsView.setVisible(false);

        activeView.setVisible(true);
        activeView.toFront(); // Đẩy view lên lớp trên cùng của StackPane
    }

    // ==========================================
    // SETUP TABLE BUTTONS
    // ==========================================
    private void setupActionColumns() {
        // 1. Nút Edit cho Tab Config
        configActionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit Config");
            {
                editBtn.getStyleClass().add("action-btn");
                editBtn.setOnAction(e -> {
                    Auction auction = getTableView().getItems().get(getIndex());
                    // TODO: Mở popup chỉnh sửa
                    System.out.println("Editing Auction: " + auction.getId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        // 2. Nút Process cho Tab Due
        dueActionCol.setCellFactory(param -> new TableCell<>() {
            private final Button processBtn = new Button("Process Result");
            {
                processBtn.getStyleClass().addAll("action-btn", "btn-process");
                processBtn.setOnAction(e -> {
                    Auction auction = getTableView().getItems().get(getIndex());
                    // TODO: Mở popup xác nhận người thắng hoặc hủy
                    System.out.println("Processing Due Auction: " + auction.getId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : processBtn);
            }
        });

        // 3. Nút Approve/Reject cho Tab Transactions (Top-up)
        topUpActionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox pane = new HBox(6, approveBtn, rejectBtn);
            {
                approveBtn.getStyleClass().add("action-btn");
                rejectBtn.getStyleClass().addAll("action-btn", "btn-reject");

                approveBtn.setOnAction(e -> {
                    TopUpRequest req = getTableView().getItems().get(getIndex());
                    System.out.println("Approved request for user: " + req.getUserId());
                });
                rejectBtn.setOnAction(e -> {
                    TopUpRequest req = getTableView().getItems().get(getIndex());
                    System.out.println("Rejected request for user: " + req.getUserId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // 4. Nút Mark as Paid cho Tab Transactions (Payment)
        paymentActionCol.setCellFactory(param -> new TableCell<>() {
            private final Button paidBtn = new Button("Mark as Paid");
            {
                paidBtn.getStyleClass().add("action-btn");
                paidBtn.setOnAction(e -> {
                    Auction auction = getTableView().getItems().get(getIndex());
                    System.out.println("Payment confirmed for auction: " + auction.getId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : paidBtn);
            }
        });
    }
}
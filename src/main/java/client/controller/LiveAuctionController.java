package client.controller;

import client.network.ClientSocket;
import client.network.ResponseHandler;
import client.manager.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Bid;
import model.User;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LiveAuctionController implements UserDataReceiver {
    private boolean historyLoaded = false;

    // ==================== FXML COMPONENTS ====================

    // Header
    @FXML private Label currentTimeLabel;

    // Product Image
    @FXML private ImageView productImageView;

    // Product Info
    @FXML private Label productNameLabel;
    @FXML private Label productDescLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label stepPriceLabel;
    @FXML private Label floorPriceLabel;
    @FXML private Label countdownLabel;

    // Leader Info
    @FXML private Label currentWinnerLabel;
    @FXML private Label winnerTimeLabel;

    // Balance
    @FXML private Label estimatedBalanceLabel;
    @FXML private ProgressBar balanceProgressBar;
    @FXML private Label maxBidLabel;

    // Bid Section
    @FXML private TextField bidAmountField;
    @FXML private Button addStepBtn;
    @FXML private Button maxBidBtn;
    @FXML private Button placeBidBtn;
    @FXML private Label bidWarningLabel;

    // Action Buttons
    @FXML private Button backHomeBtn;
    @FXML private Button leaveBtn;
    @FXML private Button refreshBtn;

    // Chart
    @FXML private LineChart<String, Number> priceChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    // Bid History
    @FXML private TableView<Bid> bidHistoryTable;
    @FXML private TableColumn<Bid, String> timeColumn;
    @FXML private TableColumn<Bid, String> bidderColumn;
    @FXML private TableColumn<Bid, String> amountColumn;
    @FXML private TableColumn<Bid, String> statusColumn;

    // ==================== VARIABLES ====================

    private ClientSocket client;
    private User currentUser;
    private String auctionId;
    private BigDecimal currentPrice;
    private BigDecimal stepPrice;
    private BigDecimal floorPrice;
    private BigDecimal myBalance;
    private Timer countdownTimer;
    private XYChart.Series<String, Number> chartSeries;
    private ObservableList<Bid> bidHistoryList;
    private int bidCounter = 0;

    // ==================== INITIALIZE ====================

    @FXML
    public void initialize() {
        instance = this;
        // Setup Table Columns
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timeString"));
        bidderColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amountString"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Khởi tạo bid history
        bidHistoryList = FXCollections.observableArrayList();
        bidHistoryTable.setItems(bidHistoryList);

        // Khởi tạo chart
        chartSeries = new XYChart.Series<>();
        chartSeries.setName("Price History");
        priceChart.getData().add(chartSeries);

        // Setup button actions
        addStepBtn.setOnAction(e -> addStepPrice());
        maxBidBtn.setOnAction(e -> setMaxBid());
        placeBidBtn.setOnAction(e -> placeBid());
        backHomeBtn.setOnAction(e -> goBackToHome());
        leaveBtn.setOnAction(e -> leaveAndGoBack());
        refreshBtn.setOnAction(e -> refreshBidHistory());

        // Start realtime clock
        startClock();

        // Đăng ký listener
        ResponseHandler.setLiveAuctionListener(this::handleServerMessage);
    }

    // ==================== SINGLETON INSTANCE ====================
    private static LiveAuctionController instance;

    public static LiveAuctionController getInstance() {
        return instance;
    }


    // ==================== NHẬN LỊCH SỬ TỪ RESPONSEHANDLER ====================
    public void loadBidHistory(List<Bid> history) {
        Platform.runLater(() -> {
            bidHistoryList.clear();
            chartSeries.getData().clear();
            bidCounter = 0;

            for (Bid bid : history) {
                addChartData(bidCounter++, bid.getAmount());
                bidHistoryList.add(bid);
            }
            bidHistoryTable.refresh();
            historyLoaded = true;
        });
    }

    // ==================== NHẬN UPDATE PRICE ====================
    public void handleUpdatePrice(String data) {
        Platform.runLater(() -> {
            String[] parts = data.split("\\|");
            if (parts.length >= 4) {
                String newPrice = parts[2];
                String bidder = parts[3];
                String bidTime = parts.length >= 5 ? parts[4] : LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                currentPrice = new BigDecimal(newPrice);
                currentPriceLabel.setText(formatPrice(currentPrice));
                currentWinnerLabel.setText(bidder);
                winnerTimeLabel.setText(bidTime);

                // Cập nhật biểu đồ
                addChartData(bidCounter++, currentPrice);

                // Cập nhật lịch sử
                Bid bid = new Bid();
                bid.setTimeString(bidTime);
                bid.setUsername(bidder);
                bid.setAmount(currentPrice);
                bid.setStatus("LEADING");
                bidHistoryList.add(0, bid);

                // Cập nhật trạng thái OUTBID cho các bid cũ
                for (int i = 1; i < bidHistoryList.size(); i++) {
                    bidHistoryList.get(i).setStatus("OUTBID");
                }
                bidHistoryTable.refresh();

                showInfo("💰 New bid: " + formatPrice(currentPrice) + " from " + bidder);
            }
        });
    }
    // ==================== SETTERS ====================

    @Override
    public void setClient(ClientSocket client) {
        this.client = client;
    }

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            this.myBalance = user.getBalance();
            updateBalanceDisplay();
        }
    }

    public void setAuctionData(String auctionId, String productName, String productDesc,
                               String currentPrice, String stepPrice, String floorPrice,
                               String endTime, String imageUrl) {
        this.auctionId = auctionId;
        this.currentPrice = new BigDecimal(currentPrice);
        this.stepPrice = new BigDecimal(stepPrice);
        this.floorPrice = new BigDecimal(floorPrice);

        productNameLabel.setText(productName);
        productDescLabel.setText(productDesc);
        currentPriceLabel.setText(formatPrice(this.currentPrice));
        stepPriceLabel.setText(formatPrice(this.stepPrice));
        floorPriceLabel.setText(formatPrice(this.floorPrice));

        // Load image
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("NO_IMAGE")) {
            try {
                Image image = new Image(imageUrl);
                productImageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Cannot load image: " + imageUrl);
            }
        }

        startCountdown(endTime);

        if (client != null) {
            client.sendJoin(auctionId);
        }
    }

    // ==================== BALANCE ====================

    private void updateBalanceDisplay() {
        if (estimatedBalanceLabel != null) {
            estimatedBalanceLabel.setText(formatPrice(myBalance));
        }
        if (maxBidLabel != null) {
            maxBidLabel.setText("Max possible bid: " + formatPrice(myBalance));
        }
        if (balanceProgressBar != null && myBalance != null) {
            double progress = myBalance.doubleValue() / 1_000_000_000.0;
            balanceProgressBar.setProgress(Math.min(progress, 1.0));
        }
    }

    // ==================== BID ACTIONS ====================

    private void addStepPrice() {
        if (bidAmountField != null && currentPrice != null && stepPrice != null) {
            BigDecimal suggestedBid = currentPrice.add(stepPrice);
            bidAmountField.setText(formatPrice(suggestedBid));
        }
    }

    private void setMaxBid() {
        if (bidAmountField != null && myBalance != null) {
            bidAmountField.setText(formatPrice(myBalance));
        }
    }

    private void placeBid() {
        if (client == null) {
            showError("Not connected to server");
            return;
        }

        String amountText = bidAmountField.getText().trim()
                .replace("USD", "")
                .replace(".", "")
                .replace(",", "")
                .trim();

        if (amountText.isEmpty()) {
            showWarning("Please enter bid amount");
            return;
        }

        BigDecimal bidAmount;
        try {
            bidAmount = new BigDecimal(amountText);
        } catch (NumberFormatException e) {
            showWarning("Invalid amount format");
            return;
        }

        BigDecimal minRequired = currentPrice.add(stepPrice);
        if (bidAmount.compareTo(minRequired) < 0) {
            showWarning("Minimum bid is " + formatPrice(minRequired));
            return;
        }

        if (myBalance != null && bidAmount.compareTo(myBalance) > 0) {
            showWarning("Insufficient balance! Your balance: " + formatPrice(myBalance));
            return;
        }

        client.sendBid(auctionId, amountText);
        bidAmountField.clear();
        hideWarning();
    }

    // ==================== NAVIGATION ====================

    private void goBackToHome() {
        System.out.println("[LiveAuction] Going back to home WITHOUT leaving room");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();

            HomePageController controller = loader.getController();
            controller.setClient(client);
            controller.setUser(currentUser);

            Stage stage = (Stage) backHomeBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Auction System");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot go back to home!");
        }
    }

    private void leaveAndGoBack() {
        System.out.println("[LiveAuction] Leaving room: " + auctionId);

        if (countdownTimer != null) {
            countdownTimer.cancel();
        }

        if (client != null) {
            client.sendLeave();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();

            HomePageController controller = loader.getController();
            controller.setClient(client);
            controller.setUser(currentUser);

            Stage stage = (Stage) leaveBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Auction System");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot go back to home!");
        }
    }

    // ==================== SERVER MESSAGES ====================

    private void handleServerMessage(String msg) {
        Platform.runLater(() -> {
            System.out.println("[LiveAuction] Received: " + msg);

            if (msg.startsWith("UPDATE_PRICE")) {
                String[] parts = msg.split("\\|");
                if (parts.length >= 5) {
                    String newPrice = parts[2];
                    String bidder = parts[3];
                    String bidTime = parts[4];

                    currentPrice = new BigDecimal(newPrice);
                    currentPriceLabel.setText(formatPrice(currentPrice));
                    currentWinnerLabel.setText(bidder);
                    winnerTimeLabel.setText(bidTime);

                    // Update balance display
                    if (myBalance != null && currentPrice != null) {
                        BigDecimal remaining = myBalance.subtract(currentPrice);
                        maxBidLabel.setText("Max possible bid: " + formatPrice(remaining));
                        double percentUsed = currentPrice.doubleValue() / myBalance.doubleValue();
                        balanceProgressBar.setProgress(Math.min(percentUsed, 1.0));
                    }

                    // Add to chart
                    addChartData(bidCounter++, currentPrice);

                    // Add to bid history
                    Bid bid = new Bid();
                    bid.setTimeString(bidTime);
                    bid.setUsername(bidder);
                    bid.setAmount(currentPrice);
                    bid.setStatus("LEADING");
                    bidHistoryList.add(0, bid);

                    // Update previous bids status
                    for (int i = 1; i < bidHistoryList.size(); i++) {
                        bidHistoryList.get(i).setStatus("OUTBID");
                    }
                    bidHistoryTable.refresh();

                    showInfo("💰 New bid: " + formatPrice(currentPrice) + " from " + bidder);
                }
            } else if (msg.startsWith("BID_SUCCESS")) {
                showInfo("✅ Bid placed successfully!");
            } else if (msg.startsWith("BID_FAILED")) {
                String errorMsg = msg.length() > 11 ? msg.substring(11) : "Bid failed";
                showError("❌ " + errorMsg);
            } else if (msg.startsWith("JOIN_SUCCESS")) {
                System.out.println("Successfully joined auction: " + auctionId);
                showInfo("✅ Joined auction successfully!");

                String[] parts = msg.split("\\|");
                if (parts.length >= 4) {
                    String currentPriceStr = parts[2];
                    String stepPriceStr = parts[3];
                    String endTimeStr = parts[4];

                    currentPrice = new BigDecimal(currentPriceStr);
                    stepPrice = new BigDecimal(stepPriceStr);
                    currentPriceLabel.setText(formatPrice(currentPrice));
                    stepPriceLabel.setText(formatPrice(stepPrice));

                    chartSeries.getData().clear();
                    addChartData(0, currentPrice);
                    startCountdown(endTimeStr);
                    bidHistoryList.clear();
                    bidHistoryTable.refresh();
                }
            } else if (msg.startsWith("ERROR")) {
                showError("Server error: " + msg);
            } else if (msg.startsWith("DISCONNECTED")) {
                showError("Disconnected from server!");
            }
        });
    }

    // ==================== CHART ====================

    private void addChartData(int index, BigDecimal price) {
        double priceInMillions = price.doubleValue() / 1_000_000;
        chartSeries.getData().add(new XYChart.Data<>(String.valueOf(index), priceInMillions));
        if (chartSeries.getData().size() > 20) {
            chartSeries.getData().remove(0);
        }
    }

    // ==================== COUNTDOWN ====================

    private void startCountdown(String endTimeStr) {
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }

        try {
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            countdownTimer = new Timer(true);
            countdownTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        Duration remaining = Duration.between(LocalDateTime.now(), endTime);
                        if (remaining.isNegative()) {
                            countdownLabel.setText("00:00");
                            countdownTimer.cancel();
                        } else {
                            long minutes = remaining.toMinutes();
                            long seconds = remaining.getSeconds() % 60;
                            countdownLabel.setText(String.format("%02d:%02d", minutes, seconds));
                        }
                    });
                }
            }, 0, 1000);
        } catch (Exception e) {
            countdownLabel.setText("--:--");
        }
    }

    private void startClock() {
        Timer clockTimer = new Timer(true);
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (currentTimeLabel != null) {
                        currentTimeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                });
            }
        }, 0, 1000);
    }

    // ==================== BID HISTORY ====================

    private void refreshBidHistory() {
        System.out.println("Refreshing bid history for auction: " + auctionId);
        showInfo("Refreshing bid history...");
        // TODO: Load bid history from server
    }

    // ==================== UTILITIES ====================

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0 USD";
        return String.format("%,.0f USD", price);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        if (bidWarningLabel != null) {
            bidWarningLabel.setText(message);
            bidWarningLabel.setVisible(true);
        }
    }

    private void hideWarning() {
        if (bidWarningLabel != null) {
            bidWarningLabel.setVisible(false);
        }
    }
}
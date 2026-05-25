package client.controller;

import client.network.ClientSocket;
import client.network.response.ResponseHandler;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LiveAuctionController implements UserDataReceiver {

    // ==================== FXML COMPONENTS ====================

    @FXML private Label currentTimeLabel;
    @FXML private ImageView productImageView;
    @FXML private Label productNameLabel;
    @FXML private Label productDescLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label stepPriceLabel;
    @FXML private Label floorPriceLabel;
    @FXML private Label countdownLabel;
    @FXML private Label currentWinnerLabel;
    @FXML private Label winnerTimeLabel;
    @FXML private Label estimatedBalanceLabel;
    @FXML private ProgressBar balanceProgressBar;
    @FXML private Label maxBidLabel;
    @FXML private TextField bidAmountField;
    @FXML private Button addStepBtn;
    @FXML private Button maxBidBtn;
    @FXML private Button placeBidBtn;
    @FXML private Label bidWarningLabel;
    @FXML private Button backHomeBtn;
    @FXML private Button leaveBtn;
    @FXML private Button refreshBtn;
    @FXML private LineChart<String, Number> priceChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private TableView<Bid> bidHistoryTable;
    @FXML private TableColumn<Bid, String> timeColumn;
    @FXML private TableColumn<Bid, String> bidderColumn;
    @FXML private TableColumn<Bid, String> amountColumn;
    @FXML private TableColumn<Bid, String> statusColumn;

    // ==================== INSTANCE VARIABLES ====================

    private ClientSocket client;
    private User currentUser;
    private String auctionId;
    private BigDecimal currentPrice;
    private BigDecimal stepPrice;
    private BigDecimal floorPrice;
    private boolean isAuctionEnded = false;

    // Balance ảo (chỉ dự kiến, không lưu database)
    private BigDecimal virtualBalance;
    private BigDecimal myPendingBid;      // Số tiền mình đang giữ chỗ (bid hiện tại)
    private BigDecimal pendingBidAmount;  // Số tiền vừa gửi lên server, chờ xác nhận

    private Timer countdownTimer;
    private XYChart.Series<String, Number> chartSeries;
    private ObservableList<Bid> bidHistoryList;
    private int bidCounter = 0;

    private static LiveAuctionController instance;

    // ==================== STATIC STATE (lưu khi Back to Home, khôi phục khi quay lại) ====================

    private static List<Bid> globalBidHistory = new ArrayList<>();
    private static int globalBidCounter = 0;
    private static BigDecimal globalCurrentPrice = BigDecimal.ZERO;
    private static String globalWinnerName = null;
    private static String globalWinnerTime = null;
    private static BigDecimal globalVirtualBalance = null;
    private static BigDecimal globalMyPendingBid = BigDecimal.ZERO;
    private static String globalAuctionId = null;

    // ==================== INITIALIZE ====================

    @FXML
    public void initialize() {
        instance = this;

        // Setup Table Columns
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timeString"));
        bidderColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amountString"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Khởi tạo bid history list
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

        // Đăng ký listener nhận message từ server
        ResponseHandler.setLiveAuctionListener(this::handleServerMessage);
    }

    // ==================== SINGLETON ====================

    public static LiveAuctionController getInstance() {
        return instance;
    }

    // ==================== SETTERS ====================

    @Override
    public void setClient(ClientSocket client) {
        this.client = client;
    }

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        // Balance ảo sẽ được khởi tạo trong setAuctionData() sau khi biết auctionId
        // Không khởi tạo ở đây để tránh race condition (setUser gọi trước setAuctionData)
        if (user != null) {
            System.out.println("🔥 setUser called for: " + user.getUsername());
        }
    }

    public void handleLiveUpdate(String raw) {
        System.out.println(raw);

    }

    public void setAuctionData(String auctionId, String productName, String productDesc,
                               String currentPrice, String stepPrice, String floorPrice,
                               String endTime, String imageUrl) {
        this.auctionId = auctionId;
        this.currentPrice = new BigDecimal(currentPrice);
        this.stepPrice = new BigDecimal(stepPrice);
        this.floorPrice = new BigDecimal(floorPrice);

        // ====== KHỞI TẠO BALANCE ẢO ======
        // Nếu quay lại cùng auction → khôi phục từ static
        if (auctionId.equals(globalAuctionId) && globalVirtualBalance != null) {
            this.virtualBalance = globalVirtualBalance;
            this.myPendingBid = (globalMyPendingBid != null) ? globalMyPendingBid : BigDecimal.ZERO;
            this.pendingBidAmount = BigDecimal.ZERO;
            System.out.println("🔥 Restored virtual balance from static: " + virtualBalance);
        } else {
            // Auction mới → dùng balance thật từ user
            if (currentUser != null && currentUser.getBalance() != null) {
                this.virtualBalance = currentUser.getBalance();
            } else {
                this.virtualBalance = BigDecimal.ZERO;
            }
            this.myPendingBid = BigDecimal.ZERO;
            this.pendingBidAmount = BigDecimal.ZERO;
            globalAuctionId = auctionId; // Chỉ cập nhật khi là auction mới
            System.out.println("🔥 New auction, init virtual balance: " + virtualBalance);
        }

        // Hiển thị thông tin sản phẩm
        productNameLabel.setText(productName);
        productDescLabel.setText(productDesc);
        currentPriceLabel.setText(formatPrice(this.currentPrice));
        stepPriceLabel.setText(formatPrice(this.stepPrice));
        floorPriceLabel.setText(formatPrice(this.floorPrice));

        updateBalanceDisplay();

        // Load image
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("NO_IMAGE")) {
            try {
                Image image = new Image(imageUrl, true);  // true = background thread
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

    // ==================== BALANCE ẢO ====================

    private void updateBalanceDisplay() {
        if (estimatedBalanceLabel != null && virtualBalance != null) {
            estimatedBalanceLabel.setText(formatPrice(virtualBalance));
        }
        if (maxBidLabel != null && virtualBalance != null) {
            maxBidLabel.setText("💰 Max possible bid: " + formatPrice(virtualBalance));
        }
        if (balanceProgressBar != null && virtualBalance != null) {
            double progress = virtualBalance.doubleValue() / 1_000_000_000.0;
            balanceProgressBar.setProgress(Math.min(progress, 1.0));
        }
    }

    // Trừ virtual balance khi đặt bid thành công
    private void deductVirtualBalance(BigDecimal amount) {
        if (virtualBalance != null && amount != null) {
            virtualBalance = virtualBalance.subtract(amount);
            myPendingBid = amount;
            // Lưu vào static để khôi phục khi quay lại
            globalVirtualBalance = virtualBalance;
            globalMyPendingBid = myPendingBid;
            updateBalanceDisplay();
            System.out.println("🔥 Virtual balance deducted: " + amount + ", remaining: " + virtualBalance);
        }
    }

    // Cộng lại virtual balance khi bị outbid
    private void refundVirtualBalance() {
        if (virtualBalance != null && myPendingBid != null && myPendingBid.compareTo(BigDecimal.ZERO) > 0) {
            virtualBalance = virtualBalance.add(myPendingBid);
            myPendingBid = BigDecimal.ZERO;
            // Lưu vào static
            globalVirtualBalance = virtualBalance;
            globalMyPendingBid = BigDecimal.ZERO;
            updateBalanceDisplay();
            System.out.println("🔥 Virtual balance refunded, new balance: " + virtualBalance);
            showToast("You were outbid! Money returned to your balance.");
        }
    }

    // ==================== BID ACTIONS ====================

    private void addStepPrice() {
        if (bidAmountField != null && currentPrice != null && stepPrice != null) {
            BigDecimal suggestedBid = currentPrice.add(stepPrice);
            bidAmountField.setText(suggestedBid.toPlainString());
        }
    }

    private void setMaxBid() {
        if (bidAmountField != null && virtualBalance != null) {
            bidAmountField.setText(virtualBalance.toPlainString());
        }
    }

    private void placeBid() {
        if (client == null) {
            showError("Not connected to server");
            return;
        }

        String amountText = bidAmountField.getText().trim()
                .replace("USD", "")
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

        if (virtualBalance != null && bidAmount.compareTo(virtualBalance) > 0) {
            showWarning("Insufficient balance! Your balance: " + formatPrice(virtualBalance));
            return;
        }

        // Lưu số tiền sẽ đặt để xử lý sau khi server xác nhận
        pendingBidAmount = bidAmount;

        client.sendBid(auctionId, amountText);
        bidAmountField.clear();
        hideWarning();
    }

    // ==================== NAVIGATION ====================

    private void goBackToHome() {
        System.out.println("[LiveAuction] Going back to home (keeping auction state)");

        // Lưu balance ảo vào static trước khi rời (KHÔNG xóa static)
        globalVirtualBalance = virtualBalance;
        globalMyPendingBid = myPendingBid;

        if (countdownTimer != null) {
            countdownTimer.cancel();
        }

        navigateToHome();
    }

    private void leaveAndGoBack() {
        System.out.println("[LiveAuction] Leaving room permanently: " + auctionId);

        // Hoàn tiền pending khi leave
        if (virtualBalance != null && myPendingBid != null
                && myPendingBid.compareTo(BigDecimal.ZERO) > 0) {
            virtualBalance = virtualBalance.add(myPendingBid);
            myPendingBid = BigDecimal.ZERO;
            updateBalanceDisplay();
            showToast("💰 Refunded " + formatPrice(myPendingBid) + " because you left.");
        }

        // Lưu balance thật (sau khi đã hoàn tiền) vào session
        if (currentUser != null && virtualBalance != null) {
            currentUser.setBalance(virtualBalance);
            UserSession.setCurrentUser(currentUser);
        }

        // Xóa toàn bộ static state (đã leave hẳn, không quay lại auction này)
        globalBidHistory.clear();
        globalBidCounter = 0;
        globalCurrentPrice = BigDecimal.ZERO;
        globalWinnerName = null;
        globalWinnerTime = null;
        globalVirtualBalance = null;
        globalMyPendingBid = BigDecimal.ZERO;
        globalAuctionId = null;

        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        if (client != null) {
            client.sendLeave();
        }

        navigateToHome();
    }

    private void navigateToHome() {
        try {
            // Lưu balance ảo vào session trước khi rời
            if (currentUser != null && virtualBalance != null) {
                currentUser.setBalance(virtualBalance);
                UserSession.setCurrentUser(currentUser);
                System.out.println("🔥 Saved virtual balance to session: " + virtualBalance);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();

            HomePageController controller = loader.getController();
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

    // ==================== SERVER MESSAGES ====================

    private void handleServerMessage(String msg) {
        Platform.runLater(() -> {
            System.out.println("[LiveAuction] Received: " + msg);

            // ==================== UPDATE PRICE ====================
            if (msg.startsWith("UPDATE_PRICE")) {
                String[] parts = msg.split("\\|");
                if (parts.length >= 5) {
                    String newPrice = parts[2];
                    String bidder = parts[3];
                    String bidTime = parts[4];
                    String winnerId = parts.length >= 6 ? parts[5] : null;

                    // Kiểm tra trùng lặp
                    boolean isDuplicate = false;
                    if (!bidHistoryList.isEmpty()) {
                        Bid lastBid = bidHistoryList.get(0);
                        if (lastBid.getTimeString().equals(bidTime)
                                && lastBid.getUsername().equals(bidder)
                                && lastBid.getAmount().compareTo(new BigDecimal(newPrice)) == 0) {
                            isDuplicate = true;
                            System.out.println("🔥 Duplicate bid ignored: " + bidTime);
                        }
                    }

                    // Cập nhật giá và winner (luôn cập nhật dù có duplicate)
                    currentPrice = new BigDecimal(newPrice);
                    currentPriceLabel.setText(formatPrice(currentPrice));
                    currentWinnerLabel.setText(bidder);
                    winnerTimeLabel.setText(bidTime);

                    // Lưu winner vào static để khôi phục khi quay lại
                    globalWinnerName = bidder;
                    globalWinnerTime = bidTime;

                    // Nếu mình bị outbid → hoàn tiền
                    if (myPendingBid != null && myPendingBid.compareTo(BigDecimal.ZERO) > 0
                            && winnerId != null && currentUser != null
                            && !winnerId.equals(currentUser.getUser_id())) {
                        refundVirtualBalance();
                    }

                    // Nếu mình là người vừa đặt giá thành công → trừ tiền
                    if (currentUser != null && bidder.equals(currentUser.getUsername())) {
                        if (pendingBidAmount != null && pendingBidAmount.compareTo(BigDecimal.ZERO) > 0) {
                            deductVirtualBalance(pendingBidAmount);
                            showToast("✅ Bid placed! " + formatPrice(pendingBidAmount) + " reserved.");
                            pendingBidAmount = null;
                        }
                    }

                    // Thêm vào lịch sử & chart (nếu không trùng)
                    if (!isDuplicate) {
                        addChartData(bidCounter++, currentPrice);

                        Bid bid = new Bid();
                        bid.setTimeString(bidTime);
                        bid.setUsername(bidder);
                        bid.setAmount(currentPrice);
                        bid.setAmountString(String.format("%,.0f", currentPrice) + " USD");
                        bid.setStatus("LEADING");
                        bidHistoryList.add(0, bid);

                        // Cập nhật trạng thái các bid cũ → OUTBID
                        for (int i = 1; i < bidHistoryList.size(); i++) {
                            bidHistoryList.get(i).setStatus("OUTBID");
                        }
                        bidHistoryTable.refresh();

                        // Lưu vào static để khôi phục khi quay lại
                        globalBidHistory = new ArrayList<>(bidHistoryList);
                        globalBidCounter = bidCounter;
                        globalCurrentPrice = currentPrice;

                        System.out.println("Saved to static - Total bids: " + globalBidHistory.size());
                    }
                }
                return;
            }

            // ==================== JOIN THÀNH CÔNG ====================
            if (msg.startsWith("JOIN_SUCCESS")) {
                System.out.println("Successfully joined auction: " + auctionId);
                showToast("Joined auction successfully!");

                String[] parts = msg.split("\\|");
                if (parts.length >= 4) {
                    currentPrice = new BigDecimal(parts[2]);
                    stepPrice = new BigDecimal(parts[3]);
                    currentPriceLabel.setText(formatPrice(currentPrice));
                    stepPriceLabel.setText(formatPrice(stepPrice));

                    if (parts.length >= 5) {
                        startCountdown(parts[4]);
                    }

                    // Khôi phục bid history & chart từ static nếu quay lại cùng auction
                    if (!globalBidHistory.isEmpty()) {
                        bidHistoryList.clear();
                        bidHistoryList.addAll(globalBidHistory);
                        bidCounter = globalBidCounter;

                        // Vẽ lại chart
                        chartSeries.getData().clear();
                        List<Bid> reversedForChart = new ArrayList<>(bidHistoryList);
                        java.util.Collections.reverse(reversedForChart);
                        for (int i = 0; i < reversedForChart.size(); i++) {
                            double priceInMillions = reversedForChart.get(i).getAmount().doubleValue() / 1_000_000;
                            chartSeries.getData().add(new XYChart.Data<>(String.valueOf(i), priceInMillions));
                        }

                        bidHistoryTable.refresh();
                        System.out.println("Restored " + bidHistoryList.size() + " bids from static");
                    } else {
                        // Nếu chưa có lịch sử, thêm điểm giá khởi điểm
                        Bid initialBid = new Bid();
                        initialBid.setTimeString(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        initialBid.setUsername("");
                        initialBid.setAmount(currentPrice);
                        initialBid.setAmountString(formatPrice(currentPrice));
                        initialBid.setStatus("LEADING");
                        bidHistoryList.add(initialBid);
                        globalBidHistory = new ArrayList<>(bidHistoryList);
                        addChartData(bidCounter++, currentPrice);
                        bidHistoryTable.refresh();
                    }

                    // Khôi phục tên winner
                    if (globalWinnerName != null) {
                        currentWinnerLabel.setText(globalWinnerName);
                        winnerTimeLabel.setText(globalWinnerTime);
                        System.out.println("Restored winner: " + globalWinnerName + " at " + globalWinnerTime);
                    }
                }

                // Lấy lịch sử bid từ server
                if (client != null && auctionId != null) {
                    client.sendGetBidHistory(auctionId);
                }
                return;
            }

            // ==================== LỊCH SỬ GIÁ TỪ SERVER ====================
            if (msg.startsWith("BID_HISTORY_SUCCESS")) {
                String data = msg.substring("BID_HISTORY_SUCCESS|".length());
                List<Bid> history = parseBidHistory(data);

                if (history != null && !history.isEmpty()) {
                    for (Bid bid : history) {
                        boolean exists = false;
                        for (Bid existing : bidHistoryList) {
                            if (existing.getTimeString().equals(bid.getTimeString())
                                    && existing.getUsername().equals(bid.getUsername())
                                    && existing.getAmount().compareTo(bid.getAmount()) == 0) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            if (bid.getAmountString() == null || bid.getAmountString().isEmpty()) {
                                bid.setAmountString(String.format("%,.0f", bid.getAmount()) + " USD");
                            }
                            bidHistoryList.add(bid);
                        }
                    }

                    // Sắp xếp mới nhất lên đầu
                    bidHistoryList.sort((a, b) -> b.getTimeString().compareTo(a.getTimeString()));

                    // Vẽ lại chart theo thứ tự thời gian
                    chartSeries.getData().clear();
                    bidCounter = 0;
                    List<Bid> chartOrder = new ArrayList<>(bidHistoryList);
                    java.util.Collections.reverse(chartOrder);
                    for (Bid bid : chartOrder) {
                        addChartData(bidCounter++, bid.getAmount());
                    }

                    // Cập nhật trạng thái LEADING / OUTBID
                    for (int i = 0; i < bidHistoryList.size(); i++) {
                        bidHistoryList.get(i).setStatus(i == 0 ? "LEADING" : "OUTBID");
                    }

                    // Cập nhật winner label từ bid mới nhất
                    if (!bidHistoryList.isEmpty()) {
                        Bid topBid = bidHistoryList.get(0);
                        if (topBid.getUsername() != null && !topBid.getUsername().isEmpty()) {
                            currentWinnerLabel.setText(topBid.getUsername());
                            winnerTimeLabel.setText(topBid.getTimeString());
                            globalWinnerName = topBid.getUsername();
                            globalWinnerTime = topBid.getTimeString();
                        }
                    }

                    bidHistoryTable.refresh();

                    // Cập nhật static
                    globalBidHistory = new ArrayList<>(bidHistoryList);
                    globalBidCounter = bidCounter;

                    System.out.println("Total bids after server sync: " + bidHistoryList.size());
                }
                return;
            }

            if (msg.startsWith("BID_HISTORY_EMPTY")) {
                System.out.println("Server returned empty bid history (keeping existing)");
                return;
            }

            // ==================== BID THẤT BẠI ====================
            if (msg.startsWith("BID_FAILED")) {
                pendingBidAmount = null;
                String errorMsg = msg.length() > 11 ? msg.substring(11) : "Bid failed";
                showError("❌ " + errorMsg);
                return;
            }

            // ==================== AUCTION KẾT THÚC ====================
            if (msg.startsWith("AUCTION_ENDED")) {
                isAuctionEnded = true;
                countdownLabel.setText("00:00:00");
                if (countdownTimer != null) {
                    countdownTimer.cancel();
                }
                placeBidBtn.setDisable(true);
                addStepBtn.setDisable(true);
                maxBidBtn.setDisable(true);
                showInfo("Auction has ended!");
                return;
            }

            // ==================== THẮNG CUỘC ====================
            if (msg.startsWith("YOU_WON")) {
                String[] parts = msg.split("\\|");
                if (parts.length >= 2) {
                    BigDecimal finalPrice = new BigDecimal(parts[1]);
                    showInfo("🎉 CONGRATULATIONS! You won the auction for " + formatPrice(finalPrice));
                } else {
                    showInfo("🎉 CONGRATULATIONS! You won the auction!");
                }
                placeBidBtn.setDisable(true);
                addStepBtn.setDisable(true);
                maxBidBtn.setDisable(true);
                return;
            }

            // ==================== ERROR / DISCONNECT ====================
            if (msg.startsWith("ERROR")) {
                showError("Server error: " + msg);
            } else if (msg.startsWith("DISCONNECTED")) {
                showError("Disconnected from server!");
            }
        });
    }

    // ==================== BID HISTORY (public, gọi từ ResponseHandler) ====================

    public void loadBidHistory(List<Bid> history) {
        Platform.runLater(() -> {
            if (history == null || history.isEmpty()) {
                System.out.println("No new bid history to load");
                return;
            }

            System.out.println("Loading " + history.size() + " bids from server");

            for (Bid bid : history) {
                if (bid.getAmountString() == null || bid.getAmountString().isEmpty()) {
                    bid.setAmountString(String.format("%,.0f", bid.getAmount()) + " USD");
                }

                boolean exists = false;
                for (Bid existing : bidHistoryList) {
                    if (existing.getTimeString().equals(bid.getTimeString())
                            && existing.getUsername().equals(bid.getUsername())
                            && existing.getAmount().compareTo(bid.getAmount()) == 0) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    bidHistoryList.add(bid);
                }
            }

            // Sắp xếp mới nhất lên đầu
            bidHistoryList.sort((a, b) -> b.getTimeString().compareTo(a.getTimeString()));

            // Vẽ lại chart
            chartSeries.getData().clear();
            bidCounter = 0;
            List<Bid> chartOrder = new ArrayList<>(bidHistoryList);
            java.util.Collections.reverse(chartOrder);
            for (Bid bid : chartOrder) {
                addChartData(bidCounter++, bid.getAmount());
            }

            // Cập nhật trạng thái LEADING / OUTBID
            for (int i = 0; i < bidHistoryList.size(); i++) {
                bidHistoryList.get(i).setStatus(i == 0 ? "LEADING" : "OUTBID");
            }

            bidHistoryTable.refresh();

            // Cập nhật static
            globalBidHistory = new ArrayList<>(bidHistoryList);
            globalBidCounter = bidCounter;

            System.out.println("Total bids: " + bidHistoryList.size());
        });
    }

    private List<Bid> parseBidHistory(String data) {
        List<Bid> history = new ArrayList<>();
        if (data == null || data.isEmpty()) return history;

        String[] bidTokens = data.split("\\|");
        for (String token : bidTokens) {
            try {
                String[] parts = token.split(";");
                if (parts.length >= 3) {
                    Bid bid = new Bid();
                    bid.setUsername(parts[0]);
                    BigDecimal amount = new BigDecimal(parts[1]);
                    bid.setAmount(amount);
                    bid.setAmountString(String.format("%,.0f", amount) + " USD");
                    bid.setTimeString(parts[2]);
                    history.add(bid);
                }
            } catch (Exception e) {
                System.err.println("Parse bid history error: " + e.getMessage());
            }
        }
        return history;
    }

    private void refreshBidHistory() {
        System.out.println("🔄 Refreshing bid history for auction: " + auctionId);
        if (client != null && auctionId != null) {
            client.sendGetBidHistory(auctionId);
        }
        showToast("Refreshing bid history...");
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
                            countdownLabel.setText("00:00:00");
                            countdownTimer.cancel();
                            if (!isAuctionEnded) {
                                isAuctionEnded = true;
                                placeBidBtn.setDisable(true);
                                addStepBtn.setDisable(true);
                                maxBidBtn.setDisable(true);
                                showInfo("Auction has ended!");
                            }
                        } else {
                            long totalSeconds = remaining.getSeconds();
                            long hours = totalSeconds / 3600;
                            long minutes = (totalSeconds % 3600) / 60;
                            long seconds = totalSeconds % 60;
                            countdownLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                        }
                    });
                }
            }, 0, 1000);
        } catch (Exception e) {
            countdownLabel.setText("--:--:--");
            System.err.println("Cannot parse end time: " + endTimeStr);
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

    // ==================== UTILITIES ====================

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0 USD";
        return String.format("%,.0f USD", price);
    }

    // Gọi từ bên ngoài để cập nhật balance (ví dụ từ ResponseHandler)
    public void updateBalance(BigDecimal newBalance) {
        Platform.runLater(() -> {
            if (virtualBalance != null) {
                this.virtualBalance = newBalance;
                globalVirtualBalance = newBalance;
                updateBalanceDisplay();
                System.out.println("[LiveAuction] Balance updated from server: " + newBalance);
            }
        });
    }

    private void showToast(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

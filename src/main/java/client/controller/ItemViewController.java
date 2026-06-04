package client.controller;

import client.manager.*;
import client.util.AlertUtils;
import client.util.TextUtils;
import client.manager.ControllerRegistry;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import javafx.stage.Stage;

import model.*;

import model.Entity.User.User;
import model.Entity.User.Role;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class ItemViewController extends BaseController
        implements AuctionStateListener  {

    private static final NumberFormat MONEY =
            NumberFormat.getCurrencyInstance(Locale.US);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    @FXML private Label lblName;
    @FXML private TextArea txtDescription;
    @FXML private Label lblStatusBadge;
    @FXML private Label lblPaymentStatus;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStep;
    @FXML private Label lblStart;
    @FXML private Label lblEnd;

    @FXML private ImageView imgItem;
    @FXML private StackPane imageContainer;

    @FXML private Button btnApproveAuction;
    @FXML private Button btnCancelAuction;
    @FXML private Button btnBack;

    @FXML private Button btnJoinAuction;
    @FXML private Button btnFavourite;

    private Auction auction;
    private int currentImageIndex = 0;
    private Timeline startWatcher;

    @FXML
    public void initialize() {
        System.out.println("Item View Loaded");
        ControllerRegistry.register(ItemViewController.class, this);
        setupImageView();
    }

    // ==================== IMAGE SETUP ====================

    private void setupImageView() {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(36);
        clip.setArcHeight(36);

        clip.widthProperty().bind(imageContainer.widthProperty());
        clip.heightProperty().bind(imageContainer.heightProperty());

        imgItem.setClip(clip);
        imgItem.setPreserveRatio(true);
        imgItem.fitWidthProperty().bind(imageContainer.widthProperty().subtract(20));
        imgItem.fitHeightProperty().bind(imageContainer.heightProperty().subtract(20));
    }

    // ==================== SET DATA ====================
    public void setAuctionData(Auction auction) {
        this.auction = auction;
        AuctionStateManager.registerListener(
                auction.getAuction_id(),
                this
        );

        if (auction.getItem() == null) return;

        currentImageIndex = 0;
        imgItem.setImage(null);

        setupFavourite();
        setupLabels();

        setupSellerUI();     // hide first
        setupAdminUI();
        setupJoinButton();   // style only if visible

        updateStatusBadge();
        scheduleStartWatcher();
        showImage(0);
    }

    // helper to load auction by id (used by admin product controller)
    public void loadAuction(String auctionId) {
        if (auctionId == null || auctionId.isBlank()) return;

        Auction a = server.service.AuctionService.getAuctionById(auctionId);
        if (a == null) {
            AlertUtils.error("Auction not found: " + auctionId);
            return;
        }

        setAuctionData(a);
    }

    @Override
    public void onDestroy() {
        if (auction != null) {
            AuctionStateManager.unregisterListener(
                    auction.getAuction_id(),
                    this
            );
        }
        if (startWatcher != null) {
            startWatcher.stop();
            startWatcher = null;
        }
    }

    private void scheduleStartWatcher() {
        if (startWatcher != null) {
            startWatcher.stop();
            startWatcher = null;
        }

        if (auction == null || auction.getStartTime() == null) return;

        // If already started, nothing to schedule
        if (!auction.getStartTime().isAfter(LocalDateTime.now())) return;

        startWatcher = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (auction.getStartTime() != null && !auction.getStartTime().isAfter(LocalDateTime.now())) {
                // start time arrived -> refresh UI and stop watcher
                setupJoinButton();
                updateStatusBadge();
                if (startWatcher != null) {
                    startWatcher.stop();
                    startWatcher = null;
                }
            } else {
                // still not started; ensure button remains disabled
                setupJoinButton();
            }
        }));

        startWatcher.setCycleCount(Animation.INDEFINITE);
        startWatcher.play();
    }

    // ==================== LABELS ====================

    private void setupLabels() {
        lblName.setText(TextUtils.safeText(auction.getItem().getName()));
        txtDescription.setText(TextUtils.safeText(auction.getItem().getDescription()));

        lblCurrentPrice.setText("Current Price: " +
                MONEY.format(auction.getCurrentPrice()));

        lblStep.setText("Min Increment: " +
                MONEY.format(auction.getMinIncrement()));

        if (auction.getStartTime() != null) {
            lblStart.setText("Start: " + auction.getStartTime().format(TIME_FMT));
        }

        if (auction.getEndTime() != null) {
            lblEnd.setText("End: " + auction.getEndTime().format(TIME_FMT));
        }
    }

    public void refreshState() {
        setupJoinButton();
        updateStatusBadge();
        setupFavourite();
    }

    // ==================== FAVORITE ====================
    private void setupFavourite() {
        boolean fav = FavouriteManager.isFavourite(
                auction.getItem().getItem_id()
        );
        updateFavouriteUI(fav);
    }

    private void updateFavouriteUI(boolean isFav) {
        if (isFav) {
            btnFavourite.setText("Remove Favourite");
            btnFavourite.setStyle("-fx-background-color:#ef4444;-fx-text-fill:white;");
        } else {
            btnFavourite.setText("Add Favourite");
            btnFavourite.setStyle("-fx-background-color:#d4af37;-fx-text-fill:black;");
        }
    }

    @FXML
    private void handleAddToFavourite() {
        User user = UserSession.getCurrentUser();
        if (user == null || auction == null) return;

        String itemId = auction.getItem().getItem_id();
        boolean fav = FavouriteManager.isFavourite(itemId);

        if (client == null) {
            AlertUtils.error("Server not connected!");
            return;
        }

        if (fav) {
            client.sendMessage("REMOVE_FAVOURITE|" + itemId);
            FavouriteManager.removeFavourite(itemId);
            updateFavouriteUI(false);
        } else {
            client.sendMessage("ADD_FAVOURITE|" + itemId);
            FavouriteManager.addFavourite(itemId);
            updateFavouriteUI(true);
        }
    }

    // ==================== STATUS (FIXED + DYNAMIC) ====================
    private void updateStatusBadge() {
        ParticipationStatus status =
                AuctionStateManager.getParticipation(
                        auction.getAuction_id()
                );

        if (status == null || status == ParticipationStatus.NOT_JOINED) {

            lblStatusBadge.setVisible(false);
            lblStatusBadge.setManaged(false);

            lblPaymentStatus.setVisible(false);
            lblPaymentStatus.setManaged(false);

            return;
        }

        lblStatusBadge.setVisible(true);
        lblStatusBadge.setManaged(true);

        switch (status) {
            case JOINED -> setBadge("Status: Joined", "#d4af37");
            case LEADING -> setBadge("Status: Leading", "#43a047");
            case OUTBID -> setBadge("Status: Outbid", "#ef4444");
            case WON     -> setBadge("Status: You won", "#43a047");
            case LOST    -> setBadge("Status: You lost", "#ef4444");
            case LEFT    -> setBadge("Status: Left auction", "#6b7280");
        }

        if (status == ParticipationStatus.WON) {
            lblPaymentStatus.setVisible(true);
            lblPaymentStatus.setManaged(true);

            if (AuctionStateManager.getPayment(auction.getAuction_id()) == PaymentStatus.PAID) {
                lblPaymentStatus.setText("Payment Status: Paid");
                lblPaymentStatus.setStyle("-fx-text-fill:#43a047;");
            } else {
                lblPaymentStatus.setText("Payment Status: Unpaid");
                lblPaymentStatus.setStyle("-fx-text-fill:#ef4444;");
            }
        } else {
            lblPaymentStatus.setVisible(false);
            lblPaymentStatus.setManaged(false);
        }
    }

    private void setBadge(String text, String color) {
        lblStatusBadge.setText(text);
        lblStatusBadge.setStyle(
                "-fx-text-fill:" + color + ";"
        );
    }

    // ==================== JOIN UI ====================

    private void setupSellerUI() {
        User user = UserSession.getCurrentUser();

        boolean isSeller =
                user != null
                        && auction.getSeller() != null
                        && user.getUser_id().equals(auction.getSeller().getUser_id());
        btnJoinAuction.setVisible(!isSeller);
        btnJoinAuction.setManaged(!isSeller);
    }


    private void setupJoinButton() {
        if (!btnJoinAuction.isVisible()) return;

        // Disable (gray-out) if auction hasn't started yet
        if (auction.getStartTime() != null && auction.getStartTime().isAfter(LocalDateTime.now())) {
            btnJoinAuction.setDisable(true);
            btnJoinAuction.setVisible(true);
            btnJoinAuction.setManaged(true);
            btnJoinAuction.setText("Not started");
            btnJoinAuction.setStyle("-fx-background-color: #9ca3af; -fx-text-fill: white;");
            return;
        } else {
            btnJoinAuction.setDisable(false);
        }

        // Ẩn nút nếu auction đã kết thúc
        if (auction.getStatus() == AuctionStatus.ENDED
                || auction.getStatus() == AuctionStatus.CANCELLED) {
            btnJoinAuction.setVisible(false);
            btnJoinAuction.setManaged(false);
            return;
        }

        boolean joined = AuctionStateManager.isJoined(auction.getAuction_id());

        if (joined) {
            ParticipationStatus status = AuctionStateManager.getParticipation(auction.getAuction_id());
            if (status == ParticipationStatus.WON) {
                btnJoinAuction.setVisible(false);
                btnJoinAuction.setManaged(false);
            } else {
                btnJoinAuction.setText("CONTINUE BIDDING");
                btnJoinAuction.setStyle("-fx-background-color: #43a047; -fx-text-fill: white;");
            }
        } else {
            btnJoinAuction.setText("JOIN AUCTION");
            btnJoinAuction.setStyle("-fx-background-color: #d4af37; -fx-text-fill: black;");
        }
    }

    private void setupAdminUI() {
        User user = UserSession.getCurrentUser();
        boolean isAdmin = user != null && user.getRole() == Role.ADMIN;

        if (!isAdmin) {
            if (btnApproveAuction != null) {
                btnApproveAuction.setVisible(false);
                btnApproveAuction.setManaged(false);
            }
            if (btnCancelAuction != null) {
                btnCancelAuction.setVisible(false);
                btnCancelAuction.setManaged(false);
            }
            if (btnBack != null) {
                btnBack.setVisible(true);
                btnBack.setManaged(true);
            }
            return;
        }

        if (btnJoinAuction != null) {
            btnJoinAuction.setVisible(false);
            btnJoinAuction.setManaged(false);
        }

        if (btnFavourite != null) {
            btnFavourite.setVisible(false);
            btnFavourite.setManaged(false);
        }

        boolean canApprove = auction != null && !auction.isCancelled() && !auction.isApproved();
        boolean canCancel = auction != null && !auction.isCancelled() && auction.getStatus() != AuctionStatus.ENDED;

        if (btnApproveAuction != null) {
            btnApproveAuction.setVisible(canApprove);
            btnApproveAuction.setManaged(canApprove);
        }
        if (btnCancelAuction != null) {
            btnCancelAuction.setVisible(canCancel);
            btnCancelAuction.setManaged(canCancel);
        }
        if (btnBack != null) {
            btnBack.setVisible(false);
            btnBack.setManaged(false);
        }
    }

    // ==================== IMAGE ====================
    private void showImage(int index) {
        List<String> images = auction.getItem().getImages();
        if (images == null || images.isEmpty()) {
            imgItem.setImage(null);
            return;
        }

        index = Math.floorMod(index, images.size());
        currentImageIndex = index;

        try {
            imgItem.setImage(new Image(images.get(index)));
        } catch (Exception e) {
            imgItem.setImage(getFallback());
        }
    }

    private Image getFallback() {
        try {
            URL url = getClass().getResource("/image/no-image.png");
            return url != null ? new Image(url.toExternalForm()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void showPreviousImage() {
        List<String> images = auction.getItem().getImages();
        if (images == null || images.isEmpty()) return;
        currentImageIndex =
                (currentImageIndex - 1 + images.size()) % images.size();
        showImage(currentImageIndex);
    }

    @FXML
    private void showNextImage() {
        List<String> images = auction.getItem().getImages();
        if (images == null || images.isEmpty()) return;
        currentImageIndex =
                (currentImageIndex + 1) % images.size();
        showImage(currentImageIndex);
    }

    // ==================== NAVIGATION ====================
    @FXML
    private void handleBack(MouseEvent event) {
        navigate(
                getStage(event),
                "/fxml/userHomePage-view.fxml",
                "Home Page"
        );
    }

    private Stage getStage(MouseEvent e) {
        return (Stage) ((Node) e.getSource())
                .getScene().getWindow();
    }

    // ==================== JOIN AUCTION ====================
    @FXML
    private void handleJoinAuction(ActionEvent event) {

        User user = UserSession.getCurrentUser();
        if (user == null || client == null) return;

        ParticipationStatus status = AuctionStateManager.getParticipation(auction.getAuction_id());
        // If user won, treat button as Pay
        if (status == ParticipationStatus.WON) {
            boolean confirmed = AlertUtils.confirm(
                    "Pay",
                    "Pay " + auction.getCurrentPrice() + " for " + auction.getItem().getName() + "?"
            );
            if (confirmed) {
                client.sendPayAuction(auction.getAuction_id());
            }
            return;
        }

        // Prevent joining if auction hasn't started yet
        if (auction.getStartTime() != null && auction.getStartTime().isAfter(LocalDateTime.now())) {
            AlertUtils.error("This auction has not started yet.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/userLiveAuction-view.fxml")
            );

            Parent root = loader.load();
            UserLiveAuctionController controller = loader.getController();

            controller.setClient(client);
            controller.setUser(user);

            controller.setAuctionData(
                    auction.getAuction_id(),
                    auction.getItem().getName(),
                    auction.getItem().getDescription(),
                    auction.getCurrentPrice().toString(),
                    auction.getMinIncrement().toString(),
                    "0",
                    auction.getEndTime() != null ? auction.getEndTime().toString() : null,
                    (auction.getItem().getImages() != null && !auction.getItem().getImages().isEmpty())
                            ? auction.getItem().getImages().getFirst()
                            : null
            );

            Stage stage = getStage(event);
            stage.setScene(new Scene(root));
            stage.setTitle("Live Auction");
            stage.show();

        } catch (IOException e) {
            AlertUtils.error("Cannot join auction");
        }
    }

    @FXML
    private void handleApproveAuction(ActionEvent event) {
        if (auction == null || client == null) return;
        if (auction.isApproved() || auction.isCancelled()) return;

        boolean confirmed = AlertUtils.confirm(
                "Approve Auction",
                "Approve this auction: " + auction.getItem().getName() + "?"
        );

        if (confirmed) {
            client.sendRequest("APPROVE_AUCTION|" + auction.getAuction_id());
        }
    }

    @FXML
    private void handleCancelAuction(ActionEvent event) {
        if (auction == null || client == null) return;
        if (auction.isCancelled()) return;

        boolean confirmed = AlertUtils.confirm(
                "Cancel Auction",
                "Cancel this auction: " + auction.getItem().getName() + "?"
        );

        if (confirmed) {
            client.sendRequest("CANCEL_AUCTION|" + auction.getAuction_id());
        }
    }

    @Override
    public void onAuctionStateChanged(String auctionId) {
        if (auction == null) {
            return;
        }

        if (!auction.getAuction_id().equals(auctionId)) {
            return;
        }

        Platform.runLater(() -> {
            setupJoinButton();
            updateStatusBadge();
        });    }
}
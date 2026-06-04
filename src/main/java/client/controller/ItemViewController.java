package client.controller;

import client.manager.*;
import client.util.AlertUtils;
import client.util.TextUtils;

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

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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

    @FXML private Button btnJoinAuction;
    @FXML private Button btnFavourite;

    private Auction auction;
    private int currentImageIndex = 0;

    @FXML
    public void initialize() {
        System.out.println("Item View Loaded");
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
        setupJoinButton();   // style only if visible

        updateStatusBadge();
        showImage(0);
    }

    @Override
    public void onDestroy() {
        if (auction != null) {
            AuctionStateManager.unregisterListener(
                    auction.getAuction_id(),
                    this
            );
        }
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

            if (AuctionStateManager.getPayment(auction.getAuction_id()) == PaymentStatus.PAID)
            {
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

        // Ẩn nút nếu auction đã kết thúc
        if (auction.getStatus() == AuctionStatus.ENDED
                || auction.getStatus() == AuctionStatus.CANCELLED) {
            btnJoinAuction.setVisible(false);
            btnJoinAuction.setManaged(false);
            return;
        }

        boolean joined = AuctionStateManager.isJoined(auction.getAuction_id());

        if (joined) {
            btnJoinAuction.setText("CONTINUE BIDDING");
            btnJoinAuction.setStyle("-fx-background-color: #43a047; -fx-text-fill: white;");
        } else {
            btnJoinAuction.setText("JOIN AUCTION");
            btnJoinAuction.setStyle("-fx-background-color: #d4af37; -fx-text-fill: black;");
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

        if (auction.getStatus() == AuctionStatus.ENDED) {
            AlertUtils.warning("Ended", "Auction ended");
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
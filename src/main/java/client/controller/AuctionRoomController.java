package client.controller;

import client.util.TextUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import model.Auction;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static client.util.NavigationUtils.showToast;

public class AuctionRoomController
        extends BaseController
        implements Initializable {

    private Auction auction;

    private int currentImageIndex = 0;

    private BigDecimal currentPrice =
            BigDecimal.ZERO;

    private BigDecimal stepPrice =
            BigDecimal.ZERO;

    @FXML
    private ImageView imgItem;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblCurrent;

    @FXML
    private Label lblStep;

    @FXML
    private Label lblCurrentPrice;

    @FXML
    private Label lblTimer;

    @FXML
    private Label lblStatus;

    @FXML
    private TextField txtBid;

    @FXML
    private Button btnBid;

    @FXML
    private VBox bidHistoryContainer;

    // ==================== INIT ====================

    @Override
    public void initialize(
            URL url,
            ResourceBundle resourceBundle
    ) {

        System.out.println(
                "Auction Room Loaded"
        );
    }

    // ==================== SET DATA ====================

    public void setAuctionData(
            Auction auction
    ) {

        if (auction == null) {
            return;
        }

        this.auction = auction;

        this.currentPrice =
                auction.getCurrentPrice();

        this.stepPrice =
                auction.getMinIncrement();

        setupAuctionInfo();

        showImage(0);
    }

    // ==================== AUCTION INFO ====================

    private void setupAuctionInfo() {

        lblItemName.setText(
                auction.getItem().getName()
        );

        updatePriceLabels();

        lblStatus.setText(
                TextUtils.toTitleCase(
                        auction.getStatus().name()
                )
        );
    }

    private void updatePriceLabels() {

        String current =
                TextUtils.formatCurrency(
                        currentPrice
                );

        String step =
                TextUtils.formatCurrency(
                        stepPrice
                );

        lblCurrent.setText(
                "Current Price: " + current
        );

        lblCurrentPrice.setText(current);

        lblStep.setText(
                "Min Increment: " + step
        );
    }

    // ==================== IMAGE ====================

    private void showImage(
            int index
    ) {

        List<String> images =
                auction.getItem().getImages();

        if (images == null || images.isEmpty()) {

            loadFallbackImage();

            return;
        }

        try {

            Image image =
                    new Image(
                            images.get(index),
                            true
                    );

            imgItem.setImage(image);

        } catch (Exception e) {

            loadFallbackImage();
        }
    }

    private void loadFallbackImage() {

        try {

            URL url =
                    getClass().getResource(
                            "/image/no-image.png"
                    );

            if (url != null) {

                imgItem.setImage(
                        new Image(
                                url.toExternalForm()
                        )
                );
            }

        } catch (Exception ignored) {
        }
    }

    @FXML
    private void showPreviousImage() {

        List<String> images =
                auction.getItem().getImages();

        if (images == null || images.isEmpty()) {
            return;
        }

        currentImageIndex--;

        if (currentImageIndex < 0) {

            currentImageIndex =
                    images.size() - 1;
        }

        showImage(currentImageIndex);
    }

    @FXML
    private void showNextImage() {

        List<String> images =
                auction.getItem().getImages();

        if (images == null || images.isEmpty()) {
            return;
        }

        currentImageIndex++;

        if (currentImageIndex >= images.size()) {

            currentImageIndex = 0;
        }

        showImage(currentImageIndex);
    }

    // ==================== BID ====================

    @FXML
    private void handleBid() {

        BigDecimal bidAmount =
                parseBidAmount();

        if (bidAmount == null) {
            return;
        }

        BigDecimal minimumBid =
                currentPrice.add(stepPrice);

        if (bidAmount.compareTo(minimumBid) < 0) {

            showError(
                    "Bid must be at least "
                            + TextUtils.formatCurrency(
                            minimumBid
                    )
            );

            return;
        }

        currentPrice = bidAmount;

        updatePriceLabels();

        addBidHistory(
                "You",
                bidAmount
        );

        txtBid.clear();

        showToast(
                (Stage) txtBid
                        .getScene()
                        .getWindow(),

                "Bid placed successfully"
        );
    }

    private BigDecimal parseBidAmount() {

        String input =
                txtBid.getText();

        if (input == null || input.isBlank()) {

            showError(
                    "Please enter bid amount."
            );

            return null;
        }

        try {

            BigDecimal amount =
                    new BigDecimal(
                            input.trim()
                    );

            if (amount.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

                showError(
                        "Bid amount must be greater than 0."
                );

                return null;
            }

            return amount;

        } catch (NumberFormatException e) {

            showError(
                    "Invalid bid amount."
            );

            return null;
        }
    }

    // ==================== BID HISTORY ====================

    private void addBidHistory(
            String username,
            BigDecimal amount
    ) {

        HBox row =
                new HBox(10);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setStyle("""
                -fx-background-color: #F8FAFC;
                -fx-padding: 10;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-border-color: #E2E8F0;
                """);

        Label userLabel =
                createUserLabel(username);

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label amountLabel =
                createAmountLabel(amount);

        row.getChildren().addAll(
                userLabel,
                spacer,
                amountLabel
        );

        bidHistoryContainer
                .getChildren()
                .add(0, row);
    }

    private Label createUserLabel(
            String username
    ) {

        Label label =
                new Label(username);

        label.setStyle("""
                -fx-font-weight: bold;
                -fx-text-fill: #0F172A;
                -fx-font-size: 14px;
                """);

        return label;
    }

    private Label createAmountLabel(
            BigDecimal amount
    ) {

        Label label =
                new Label(
                        TextUtils.formatCurrency(
                                amount
                        )
                );

        label.setStyle("""
                -fx-text-fill: #2563EB;
                -fx-font-weight: bold;
                -fx-font-size: 14px;
                """);

        return label;
    }

    // ==================== BACK ====================

    @FXML
    private void handleBack(
            ActionEvent event
    ) {

        navigate(
                (Stage) btnBid
                        .getScene()
                        .getWindow(),

                "/fxml/HomePage.fxml",

                "Home"
        );
    }
}


package client.controller;

import client.manager.FavouriteManager;
import client.manager.UserSession;

import client.network.ClientSocket;

import client.util.NavigationUtils;
import client.util.TextUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseEvent;

import javafx.scene.shape.Rectangle;

import javafx.stage.Stage;

import model.Auction;
import model.User;

import java.io.IOException;

import java.net.URL;

import java.text.NumberFormat;

import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

public class ItemViewController
        extends BaseController {

    // ==================== CONSTANTS ====================

    private static final double IMAGE_WIDTH = 380;

    private static final double IMAGE_HEIGHT = 340;

    private static final NumberFormat MONEY_FORMAT =
            NumberFormat.getCurrencyInstance(Locale.US);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    // ==================== VARIABLES ====================

    private Auction auction;

    private int currentImageIndex = 0;

    // ==================== FXML ====================

    @FXML
    private Label lblName;

    @FXML
    private TextArea txtDescription;

    @FXML
    private Label lblCurrentPrice;

    @FXML
    private Label lblStep;

    @FXML
    private Label lblStart;

    @FXML
    private Label lblEnd;

    @FXML
    private ImageView imgItem;

    @FXML
    private Button btnJoinAuction;

    @FXML
    private Button btnFavourite;

    // ==================== INITIALIZE ====================

    @FXML
    public void initialize() {

        setupImageView();
    }

    private void setupImageView() {

        Rectangle clip = new Rectangle();

        clip.setArcWidth(36);

        clip.setArcHeight(36);

        clip.widthProperty().bind(
                imgItem.fitWidthProperty()
        );

        clip.heightProperty().bind(
                imgItem.fitHeightProperty()
        );

        imgItem.setClip(clip);

        // ===== AUTO CROP =====
        imgItem.setPreserveRatio(false);

        imgItem.setSmooth(true);

        // Quan trọng để crop thay vì bóp ảnh
        imgItem.setViewport(null);
    }

    // ==================== SET DATA ====================

    public void setAuctionData(
            Auction auction
    ) {

        this.auction = auction;

        if (auction == null
                || auction.getItem() == null) {

            return;
        }

        setupFavourite();

        setupLabels();

        setupSellerUI();

        currentImageIndex = 0;

        showImage(currentImageIndex);
    }

    private void setupFavourite() {

        String itemId =
                auction.getItem()
                        .getItem_id();

        boolean isFavourite =
                FavouriteManager.isFavourite(itemId);

        updateFavouriteUI(isFavourite);
    }

    private void setupLabels() {

        lblName.setText(
                TextUtils.safeText(
                        auction.getItem().getName()
                )
        );

        txtDescription.setText(
                TextUtils.safeText(
                        auction.getItem().getDescription()
                )
        );

        lblCurrentPrice.setText(
                TextUtils.withLabel(
                        "Current Price",
                        MONEY_FORMAT.format(
                                auction.getCurrentPrice()
                        )
                )
        );

        lblStep.setText(
                TextUtils.withLabel(
                        "Min Increment",
                        MONEY_FORMAT.format(
                                auction.getMinIncrement()
                        )
                )
        );

        lblStart.setText(
                "Start Time: "
                        + auction.getStartTime()
                        .format(TIME_FORMAT)
        );

        lblEnd.setText(
                "End Time: "
                        + auction.getEndTime()
                        .format(TIME_FORMAT)
        );
    }

    private void setupSellerUI() {

        User currentUser =
                UserSession.getCurrentUser();

        boolean isSeller =
                currentUser != null
                        && auction.getSeller_Id() != null
                        && auction.getSeller_Id().equals(
                        currentUser.getUser_id()
                );

        btnJoinAuction.setVisible(!isSeller);

        btnJoinAuction.setManaged(!isSeller);
    }

    // ==================== FAVORITE ====================

    private void updateFavouriteUI(
            boolean isFavourite
    ) {

        if (isFavourite) {

            btnFavourite.setText(
                    "Remove From Favourite"
            );

            btnFavourite.setStyle("""
                -fx-background-color: #EF4444;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 12;
            """);

        } else {

            btnFavourite.setText(
                    "Add To Favourite"
            );

            btnFavourite.setStyle("""
                -fx-background-color: #d4af37;
                -fx-text-fill: black;
                -fx-font-weight: bold;
                -fx-background-radius: 12;
            """);
        }
    }

    @FXML
    private void handleAddToFavourite() {

        User currentUser =
                UserSession.getCurrentUser();

        if (currentUser == null) {

            showError("Please login again!");

            return;
        }

        if (auction == null
                || auction.getItem() == null) {

            showError("Invalid auction data!");

            return;
        }

        String itemId =
                auction.getItem()
                        .getItem_id();

        boolean isFavourite =
                FavouriteManager.isFavourite(itemId);

        if (client == null) {

            showError("Server not connected!");

            return;
        }

        if (isFavourite) {

            client.sendMessage(
                    "REMOVE_FAVOURITE|" + itemId
            );

            FavouriteManager.removeFavourite(itemId);

            updateFavouriteUI(false);

        } else {

            client.sendMessage(
                    "ADD_FAVOURITE|" + itemId
            );

            FavouriteManager.addFavourite(itemId);

            updateFavouriteUI(true);
        }
    }

    // ==================== IMAGE ====================

    private void showImage(
            int index
    ) {

        List<String> images =
                auction.getItem()
                        .getImages();

        if (images == null
                || images.isEmpty()) {

            imgItem.setImage(
                    getFallbackImage()
            );

            return;
        }

        if (index < 0) {
            index = images.size() - 1;
        }

        if (index >= images.size()) {
            index = 0;
        }

        currentImageIndex = index;

        String imagePath =
                images.get(index);

        try {

            Image image =
                    new Image(
                            imagePath,
                            IMAGE_WIDTH,
                            IMAGE_HEIGHT,
                            false,
                            true,
                            true
                    );

            imgItem.setImage(image);

        } catch (Exception e) {

            System.out.println(
                    "Image load failed: "
                            + e.getMessage()
            );

            imgItem.setImage(
                    getFallbackImage()
            );
        }
    }

    private Image getFallbackImage() {

        try {

            URL url =
                    getClass().getResource(
                            "/image/no-image.png"
                    );

            return url != null
                    ? new Image(url.toExternalForm())
                    : null;

        } catch (Exception e) {

            return null;
        }
    }

    @FXML
    private void showPreviousImage() {

        List<String> images =
                auction.getItem()
                        .getImages();

        if (images == null
                || images.isEmpty()) {

            return;
        }

        currentImageIndex =
                (currentImageIndex - 1 + images.size())
                        % images.size();

        showImage(currentImageIndex);
    }

    @FXML
    private void showNextImage() {

        List<String> images =
                auction.getItem()
                        .getImages();

        if (images == null
                || images.isEmpty()) {

            return;
        }

        currentImageIndex =
                (currentImageIndex + 1)
                        % images.size();

        showImage(currentImageIndex);
    }

    // ==================== BACK ====================

    @FXML
    private void handleBack(
            MouseEvent event
    ) {

        navigate(
                getStage(event),
                "/fxml/HomePage.fxml",
                "Home Page"
        );
    }

    // ==================== JOIN AUCTION ====================

    @FXML
    private void handleJoinAuction(
            MouseEvent event
    ) {

        if (auction == null) {
            return;
        }

        User user =
                UserSession.getCurrentUser();

        if (client == null
                || user == null) {

            showError("Cannot join auction!");

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/liveAuction-view.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            LiveAuctionController controller =
                    loader.getController();

            controller.setClient(client);

            controller.setUser(user);

            controller.setAuctionData(
                    auction.getAuction_id(),
                    auction.getItem().getName(),
                    auction.getItem().getDescription(),
                    auction.getCurrentPrice().toString(),
                    auction.getMinIncrement().toString(),
                    "0",
                    auction.getEndTime().toString(),

                    auction.getItem().getImages() != null
                            && !auction.getItem()
                            .getImages()
                            .isEmpty()

                            ? auction.getItem()
                              .getImages()
                              .getFirst()

                            : null
            );

            Stage stage =
                    getStage(event);

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Live Auction"
            );

            stage.show();

        } catch (IOException e) {

            showError(
                    "Cannot join auction."
            );
        }
    }

    // ==================== HELPERS ====================

    private Stage getStage(
            MouseEvent event
    ) {

        return (Stage)
                ((Node) event.getSource())
                        .getScene()
                        .getWindow();
    }
}
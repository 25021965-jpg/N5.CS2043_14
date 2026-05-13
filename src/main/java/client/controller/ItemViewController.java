package client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.lang.String;

import client.manager.FavouriteManager;

import model.User;
import model.Auction;

import java.io.IOException;

public class ItemViewController {
    private Auction auction;
    private int currentImageIndex = 0;
    private User currentUser;
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

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


    public void setAuctionData(Auction auction) {

        this.auction = auction;

        // tên item
        lblName.setText(auction.getItem().getName());

        // mô tả
        txtDescription.setText(
                auction.getItem().getDescription()
        );

        // giá
        lblCurrentPrice.setText(
                "Current Price: $" + auction.getCurrentPrice()
        );

        // bước giá
        lblStep.setText(
                "Min Increment: $" + auction.getMinIncrement()
        );

        // thời gian
        lblStart.setText(
                "Start Time: " + auction.getStartTime()
        );

        lblEnd.setText(
                "End Time: " + auction.getEndTime()
        );
        currentImageIndex = 0;
        showImage(currentImageIndex);

        // Ẩn nút join nếu là chủ auction
        if (
                currentUser != null &&
                        auction.getSellerId().equals(
                                currentUser.getId()
                        )        ) {

            btnJoinAuction.setVisible(false);
            btnJoinAuction.setManaged(false);
        }

    }
    private void showImage(int index) {
        java.util.List<String> images = auction.getItem().getImages();
        if (images != null && !images.isEmpty()) {
            Image image = new Image(images.get(index));
            imgItem.setImage(image);
        }
    }

    @FXML
    private void showPreviousImage() {
        java.util.List<String> images = auction.getItem().getImages();
        if (images == null || images.isEmpty()) return;

        currentImageIndex--;
        // Nếu lùi quá ảnh đầu thì quay về ảnh cuối
        if (currentImageIndex < 0) {
            currentImageIndex = images.size() - 1;
        }
        showImage(currentImageIndex);
    }

    @FXML
    private void showNextImage() {
        java.util.List<String> images = auction.getItem().getImages();
        if (images == null || images.isEmpty()) return;

        currentImageIndex++;
        // Nếu quá ảnh cuối thì quay về ảnh đầu
        if (currentImageIndex >= images.size()) {
            currentImageIndex = 0;
        }
        showImage(currentImageIndex);
    }
    @FXML
    private void handleAddToFavourite(ActionEvent event) {

        boolean isFavourite =
                FavouriteManager
                        .favouriteAuctions
                        .contains(auction);

        Alert alert;

        if (isFavourite) {

            FavouriteManager
                    .favouriteAuctions
                    .remove(auction);

            btnFavourite.setText(
                    "Add To Favourite"
            );
            btnFavourite.setStyle(
                    "-fx-background-color: #d4af37;" +
                            "-fx-text-fill: black;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-font-size: 15px;" +
                            "-fx-cursor: hand;"
            );

            alert = new Alert(
                    Alert.AlertType.INFORMATION
            );

            alert.setHeaderText(null);
            alert.setContentText(
                    "Removed from favourites!"
            );

        } else {

            FavouriteManager
                    .favouriteAuctions
                    .add(auction);

            btnFavourite.setText(
                    "Remove From Favourite"
            );
            btnFavourite.setStyle(
                    "-fx-background-color: #EF4444;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-font-size: 15px;" +
                            "-fx-cursor: hand;"
            );

            alert = new Alert(
                    Alert.AlertType.INFORMATION
            );

            alert.setHeaderText(null);
            alert.setContentText(
                    "Added to favourites!"
            );
        }

        alert.showAndWait();
    }

    @FXML
    private void handleBack(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/HomePage.fxml")
            );

            Stage stage = (Stage)
                    ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleJoinAuction(ActionEvent event) {
        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/auctionRoom-view.fxml")
            );

            Stage stage = (Stage)
                    ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
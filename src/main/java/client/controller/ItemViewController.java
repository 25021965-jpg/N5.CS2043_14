package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import client.util.NavigationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import client.manager.FavouriteManager;

import model.User;
import model.Auction;

import java.io.IOException;
import java.util.List;

public class ItemViewController {

    private Auction auction;
    private int currentImageIndex = 0;

    @FXML private Label lblName;
    @FXML private TextArea txtDescription;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStep;
    @FXML private Label lblStart;
    @FXML private Label lblEnd;
    @FXML private ImageView imgItem;
    @FXML private Button btnJoinAuction;
    @FXML private Button btnFavourite;

    // ================= SET DATA =================
    public void setAuctionData(Auction auction) {
        this.auction = auction;

        if (auction == null || auction.getItem() == null) return;

        String itemId = auction.getItem().getItem_id();

        boolean isFavourite =
                FavouriteManager.isFavourite(itemId);

        updateFavouriteUI(isFavourite);

        lblName.setText(auction.getItem().getName());
        txtDescription.setText(auction.getItem().getDescription());
        lblCurrentPrice.setText("Current Price: $" + auction.getCurrentPrice());
        lblStep.setText("Min Increment: $" + auction.getMinIncrement());
        lblStart.setText("Start Time: " + auction.getStartTime());
        lblEnd.setText("End Time: " + auction.getEndTime());

        currentImageIndex = 0;
        showImage(currentImageIndex);

        User currentUser = UserSession.getCurrentUser();

        if (currentUser != null
                && auction.getSeller_Id() != null
                && auction.getSeller_Id().equals(currentUser.getUser_id())) {

            btnJoinAuction.setVisible(false);
            btnJoinAuction.setManaged(false);
        }
    }

    // ================= FAVORITE UI =================
    private void updateFavouriteUI(boolean isFavourite) {
        if (isFavourite) {
            btnFavourite.setText("Remove From Favourite");
            btnFavourite.setStyle(
                    "-fx-background-color: #EF4444; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 12;"
            );
        } else {
            btnFavourite.setText("Add To Favourite");
            btnFavourite.setStyle(
                    "-fx-background-color: #d4af37; " +
                            "-fx-text-fill: black; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 12;"
            );
        }
    }

    // ================= IMAGE =================
    private void showImage(int index) {
        List<String> images = auction.getItem().getImages();

        if (images == null || images.isEmpty()) {
            imgItem.setImage(getFallbackImage());
            return;
        }

        if (index < 0) index = images.size() - 1;
        if (index >= images.size()) index = 0;

        this.currentImageIndex = index;
        String path = images.get(index);

        try {
            Image img = new Image(path, true);
            imgItem.setImage(img);

            System.out.println("Displaying image: " + (index + 1) + "/" + images.size());
        } catch (Exception e) {
            System.err.println("Load failed: " + path);
            imgItem.setImage(getFallbackImage());
        }
    }

    private Image getFallbackImage() {
        try {
            return new Image(getClass().getResourceAsStream("/image/no-image.png"));
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void showPreviousImage() {
        List<String> images = auction.getItem().getImages();
        if (images == null || images.isEmpty()) return;

        currentImageIndex = (currentImageIndex - 1 + images.size()) % images.size();
        showImage(currentImageIndex);
    }

    @FXML
    private void showNextImage() {
        List<String> images = auction.getItem().getImages();
        if (images == null || images.isEmpty()) return;

        currentImageIndex = (currentImageIndex + 1) % images.size();
        showImage(currentImageIndex);
    }

    // ================= FAVORITE ACTION =================
    @FXML
    private void handleAddToFavourite(ActionEvent event) {

        User currentUser = UserSession.getCurrentUser();

        if (currentUser == null) {
            NavigationUtils.showError("Please login again!");
            return;
        }

        if (auction == null || auction.getItem() == null) {
            NavigationUtils.showError("Invalid auction data!");
            return;
        }

        String itemId = auction.getItem().getItem_id();

        boolean isFavourite =
                FavouriteManager.isFavourite(itemId);

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        ClientSocket client = ClientSocket.getInstance();

        if (client == null) {
            NavigationUtils.showError("Server not connected!");
            return;
        }

        // ================= REMOVE =================
        if (isFavourite) {

            client.sendMessage(
                    "REMOVE_FAVOURITE|" + itemId
            );

            FavouriteManager.removeFavourite(itemId);
            updateFavouriteUI(false);

        }
        else {

            client.sendMessage(
                    "ADD_FAVOURITE|" + itemId
            );

            FavouriteManager.addFavourite(itemId);
            updateFavouriteUI(true);
        }
    }

    // ================= BACK =================
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // ================= JOIN AUCTION =================
    @FXML
    private void handleJoinAuction(ActionEvent event) {

        if (auction == null) return;

        ClientSocket client = ClientSocket.getInstance();
        User user = UserSession.getCurrentUser();

        if (client == null || user == null) {
            NavigationUtils.showError("Cannot join auction!");
            return;
        }

        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/fxml/liveAuction-view.fxml"));

            Parent root = loader.load();

            LiveAuctionController controller = loader.getController();

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
                    auction.getItem().getImages() != null && !auction.getItem().getImages().isEmpty()
                            ? auction.getItem().getImages().get(0)
                            : null
            );

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Live Auction");

        } catch (Exception e) {
            NavigationUtils.showError("Cannot join auction: " + e.getMessage());
        }
    }
}
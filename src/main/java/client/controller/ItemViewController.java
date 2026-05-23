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
import javafx.scene.Node;

import client.manager.FavouriteManager;

import model.User;
import model.Auction;

import java.io.IOException;
import java.util.List;

public class ItemViewController {

    private Auction auction;
    private int currentImageIndex = 0;
    private User currentUser;

    @FXML private Label lblName;
    @FXML private TextArea txtDescription;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStep;
    @FXML private Label lblStart;
    @FXML private Label lblEnd;
    @FXML private ImageView imgItem;
    @FXML private Button btnJoinAuction;
    @FXML private Button btnFavourite;

    // ==================== SETTERS ====================

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setAuctionData(Auction auction) {
        this.auction = auction;

        // Tên item
        lblName.setText(auction.getItem().getName());

        // Mô tả
        txtDescription.setText(auction.getItem().getDescription());

        // Giá
        lblCurrentPrice.setText("Current Price: $" + auction.getCurrentPrice());

        // Bước giá
        lblStep.setText("Min Increment: $" + auction.getMinIncrement());

        // Thời gian
        lblStart.setText("Start Time: " + auction.getStartTime());
        lblEnd.setText("End Time: " + auction.getEndTime());

        currentImageIndex = 0;
        showImage(currentImageIndex);

        // Ẩn nút join nếu là chủ auction
        if (currentUser != null && auction.getSeller_Id() != null
                && auction.getSeller_Id().equals(currentUser.getUser_id())) {
            btnJoinAuction.setVisible(false);
            btnJoinAuction.setManaged(false);
        } else {
            btnJoinAuction.setVisible(true);
            btnJoinAuction.setManaged(true);
        }
    }

    // ==================== IMAGE NAVIGATION ====================

    private void showImage(int index) {
        List<String> images = auction.getItem().getImages();
        if (images != null && !images.isEmpty()) {
            try {
                Image image = new Image(images.get(index));
                imgItem.setImage(image);
            } catch (Exception e) {
                System.err.println("Cannot load image: " + images.get(index));
            }
        }
    }

    @FXML
    private void showPreviousImage() {
        List<String> images = auction.getItem().getImages();
        if (images == null || images.isEmpty()) return;

        currentImageIndex--;
        if (currentImageIndex < 0) {
            currentImageIndex = images.size() - 1;
        }
        showImage(currentImageIndex);
    }

    @FXML
    private void showNextImage() {
        List<String> images = auction.getItem().getImages();
        if (images == null || images.isEmpty()) return;

        currentImageIndex++;
        if (currentImageIndex >= images.size()) {
            currentImageIndex = 0;
        }
        showImage(currentImageIndex);
    }

    // ==================== FAVOURITE ====================

    @FXML
    private void handleAddToFavourite(ActionEvent event) {
        boolean isFavourite = FavouriteManager.favouriteAuctions.contains(auction);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        if (isFavourite) {
            FavouriteManager.favouriteAuctions.remove(auction);
            btnFavourite.setText("Add To Favourite");
            btnFavourite.setStyle("-fx-background-color: #d4af37; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 12; -fx-font-size: 15px; -fx-cursor: hand;");
            NavigationUtils.showToast(stage, "Removed from favourite!");
        } else {
            FavouriteManager.favouriteAuctions.add(auction);
            btnFavourite.setText("Remove From Favourite");
            btnFavourite.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-font-size: 15px; -fx-cursor: hand;");
            NavigationUtils.showToast(stage, "Added to favourite!");
        }
    }

    // ==================== BACK TO HOME ====================

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/HomePage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // ==================== JOIN AUCTION (QUAN TRỌNG) ====================

    @FXML
    private void handleJoinAuction(ActionEvent event) {
        if (auction == null) {
            System.err.println("Auction data is null");
            return;
        }

        // 🔥 LẤY CLIENT TỪ SINGLETON NẾU this.client NULL
        ClientSocket clientToUse = ClientSocket.getInstance();
            System.out.println("🔥 ItemViewController: Retrieved client from singleton");


        if (clientToUse == null) {
            System.err.println("Client is null!");
            NavigationUtils.showError("Cannot connect to server!");
            return;
        }

        // 🔥 LẤY USER TỪ SESSION NẾU currentUser NULL
        User userToUse = this.currentUser;
        if (userToUse == null) {
            userToUse = UserSession.getCurrentUser();
            System.out.println("🔥 ItemViewController: Retrieved user from session: " + (userToUse != null ? userToUse.getUsername() : "null"));
        }

        if (userToUse == null) {
            System.err.println("User is null!");
            NavigationUtils.showError("Please login again!");
            return;
        }

        System.out.println("🔥 ItemViewController: User balance = " + userToUse.getBalance());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/liveAuction-view.fxml"));
            Parent root = loader.load();

            LiveAuctionController controller = loader.getController();
            controller.setClient(clientToUse);
            controller.setUser(userToUse);  // 🔥 Truyền user có balance
            controller.setAuctionData(
                    auction.getAuction_id(),
                    auction.getItem().getName(),
                    auction.getItem().getDescription(),
                    auction.getCurrentPrice().toString(),
                    auction.getMinIncrement().toString(),
                    "0",
                    auction.getEndTime().toString(),
                    auction.getItem().getImages() != null && !auction.getItem().getImages().isEmpty()
                            ? auction.getItem().getImages().get(0) : null
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Live Auction - " + auction.getItem().getName());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            NavigationUtils.showError("Cannot join auction: " + e.getMessage());
        }
    }
}
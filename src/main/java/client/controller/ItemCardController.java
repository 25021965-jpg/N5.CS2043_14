package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.*;

import java.io.IOException;

public class ItemCardController {

    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblCategory;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStep;
    @FXML private Label lblEndTime;

    private Auction auction;
    private ClientSocket client;
    private User currentUser;

    // ==================== SETTERS ====================
    public void setClient(ClientSocket client) {
        this.client = client;
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    public void setData(Auction auction) {
        this.auction = auction;
        Item item = auction.getItem();

        lblName.setText("NAME: " + item.getName());

        if (item.getCategory() != null) {
            String raw = item.getCategory().name().replace("_", " ").toLowerCase();
            String[] words = raw.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
            lblCategory.setText("Category: " + sb.toString().trim());
        } else {
            lblCategory.setText("Category: —");
        }

        lblCurrentPrice.setText("Current Price: " + auction.getCurrentPrice());
        lblStep.setText("Step: " + auction.getMinIncrement());

        if (item.getImages() != null && !item.getImages().isEmpty()) {
            try {
                String firstImagePath = item.getImages().get(0);
                Image image = new Image(firstImagePath);
                if (!image.isError()) {
                    imgProduct.setImage(image);
                } else {
                    System.out.println("Image loading error: " + firstImagePath);
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        if (auction.getEndTime() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            lblEndTime.setText("End at: " + auction.getEndTime().format(formatter));
        }
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/items-view.fxml"));
            Parent root = loader.load();

            ItemViewController controller = loader.getController();

            // 🔥 QUAN TRỌNG: In ra để kiểm tra
            System.out.println("🔥 ItemCardController: currentUser = " + (this.currentUser != null ? this.currentUser.getUsername() : "null"));
            System.out.println("🔥 ItemCardController: client = " + (this.client != null ? "not null" : "null"));

            controller.setCurrentUser(this.currentUser);  // Dùng this.currentUser
            controller.setClient(this.client);
            controller.setAuctionData(this.auction);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
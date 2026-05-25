package client.controller;

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ItemCardController {

    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblCategory;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStep;
    @FXML private Label lblEndTime;

    private Parent root;
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    private Auction auction;
    private ClientSocket client;

    // ==================== SETTERS ====================
    public void setClient(ClientSocket client) {
        this.client = client;
    }

    public void setData(Auction auction) {
        this.auction = auction;
        if (auction == null || auction.getItem() == null) return;
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
                String imagePath = item.getImages().getFirst();
                Image image = IMAGE_CACHE.get(imagePath);

                if (image == null) {
                    // KIỂM TRA NẾU LÀ LINK Cloudinary
                    if (imagePath.startsWith("http")) {
                        image = new Image(imagePath, 300, 200, true, true);
                    } else {
                        // NẾU LÀ FILE CỤC BỘ THÌ MỚI DÙNG FILE
                        File file = new File(imagePath);
                        if (file.exists()) {
                            image = new Image(file.toURI().toString(), 300, 200, true, true);
                        } else {
                            image = getFallbackImage();
                        }
                    }
                    IMAGE_CACHE.put(imagePath, image);
                }
                imgProduct.setImage(image);

            } catch (Exception e) {
                System.out.println("Image error: " + e.getMessage());
                imgProduct.setImage(getFallbackImage());
            }

        } else {
            imgProduct.setImage(getFallbackImage());
        }

        if (auction.getEndTime() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            lblEndTime.setText("End at: " + auction.getEndTime().format(formatter));
        }
    }

    private Image getFallbackImage() {
        URL url = getClass().getResource("/image/no-image.png");
        if (url != null) {
            return new Image(url.toExternalForm());
        }
        System.out.println("Missing resource: /image/no-image.png");
        return null;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }
    public Parent getRoot() {
        return root;
    }


    @FXML
    private void handleCardClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/items-view.fxml"));
            Parent root = loader.load();

            ItemViewController controller = loader.getController();

            System.out.println("ItemCardController: client = " + (this.client != null ? "not null" : "null"));

            controller.setAuctionData(this.auction);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
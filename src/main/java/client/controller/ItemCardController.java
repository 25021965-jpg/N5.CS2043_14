package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import javafx.scene.image.ImageView;
import model.*;
import java.io.File;

public class ItemCardController {
    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStep;
    @FXML private Label lblEndTime;

    public void setData(Auction auction) {
        Item item = auction.getItem();

        lblName.setText("NAME: " + item.getName());

        lblCurrentPrice.setText("Current Price: " + auction.getCurrentPrice());

        lblStep.setText("Step: " + auction.getMinIncrement());

        if (auction.getEndTime() != null) {
            // Định dạng lại thời gian
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            lblEndTime.setText("End at: " + auction.getEndTime().format(formatter));
        }

        if (item.getImages() != null &&
                !item.getImages().isEmpty()) {

            Image image =
                    new Image(
                            item.getImages()
                    );

            imgProduct.setImage(image);
        }
    }
}
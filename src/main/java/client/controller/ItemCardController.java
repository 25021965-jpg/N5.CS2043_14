package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import javafx.scene.image.ImageView;
import model.AuctionItem;
import model.Item;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class ItemCardController {
    @FXML private ImageView imgProduct;
    @FXML private Label lblName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStep;
    @FXML private Label lblEndTime;

    public void setData(AuctionItem item) {
        lblName.setText("NAME: " + item.getName());
        lblCurrentPrice.setText("Current Price: " + item.getCurrentPrice());
        lblStep.setText("Step: " + item.getStep());
        lblEndTime.setText("End at: " + item.getEndTime());

        if (item.getImagePath() != null) {
            File file = new File(item.getImagePath());
            Image image = new Image(file.toURI().toString());
            imgProduct.setImage(image);
        }
    }
}
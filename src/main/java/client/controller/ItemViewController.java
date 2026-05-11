package client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Node;

import model.Auction;

import java.io.IOException;

public class ItemViewController {
    private Auction auction;
    private int currentImageIndex = 0;

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
        // 1. Thêm vào danh sách tĩnh
        /*if (!FavouriteManager.favouriteAuctions.contains(this.auction)) {  //model lưu trữ
            FavouriteManager.favouriteAuctions.add(this.auction);

            // 2. Hiện thông báo thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Add success!");
            alert.showAndWait();
        } else {
            // Thông báo nếu đã có rồi (tùy chọn) */
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("This item is already in your favourites!");
            alert.showAndWait();
     //   }
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
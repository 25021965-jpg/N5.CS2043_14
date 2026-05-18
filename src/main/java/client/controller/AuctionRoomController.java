package client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Auction;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class AuctionRoomController implements Initializable {
    private long currentPrice;
    private long stepPrice;
    private Auction auction;
    private int currentImageIndex = 0;


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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setAuctionData(Auction auction) {

        this.auction = auction;
        // tên item
        lblItemName.setText(auction.getItem().getName());

        // giá
        lblCurrent.setText(
                "Current Price: $" + auction.getCurrentPrice()
        );

        // bước giá
        lblStep.setText(
                "Min Increment: $" + auction.getMinIncrement()
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

    private void handleBid() {

        try {

            String input = txtBid.getText().trim();

            if (input.isEmpty()) {
                showAlert("Please enter bid amount.");
                return;
            }

            long bidAmount = Long.parseLong(input);

            // phải lớn hơn current + step
            if (bidAmount < currentPrice + stepPrice) {

                showAlert(
                        "Bid must be at least "
                                + formatMoney(currentPrice + stepPrice)
                );

                return;
            }

            // cập nhật giá
            currentPrice = bidAmount;

            updatePriceLabels();

            // thêm lịch sử
            addBidHistory("You", bidAmount);

            // clear ô nhập
            txtBid.clear();

        } catch (NumberFormatException e) {

            showAlert("Invalid number.");

        }
    }


    private void updatePriceLabels() {

        lblCurrent.setText(formatMoney(currentPrice));
        lblCurrentPrice.setText(formatMoney(currentPrice));
        lblStep.setText(formatMoney(stepPrice));

    }


    private void addBidHistory(String username, long amount) {

        HBox row = new HBox();

        row.setSpacing(10);
        row.setAlignment(Pos.CENTER_LEFT);

        row.setStyle("""
            -fx-background-color: #F8FAFC;
            -fx-padding: 10;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #E2E8F0;
        """);

        Label userLabel = new Label(username);

        userLabel.setStyle("""
            -fx-font-weight: bold;
            -fx-text-fill: #0F172A;
            -fx-font-size: 14px;
        """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(formatMoney(amount));

        amountLabel.setStyle("""
            -fx-text-fill: #2563EB;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
        """);

        row.getChildren().addAll(userLabel, spacer, amountLabel);

        // add lên đầu danh sách
        bidHistoryContainer.getChildren().add(0, row);
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

    private String formatMoney(long amount) {

        NumberFormat format =
                NumberFormat.getInstance(new Locale("vi", "VN"));

        return format.format(amount) + " VNĐ";
    }

    private void showAlert(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
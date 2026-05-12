package client.controller;

import client.network.ClientSocket;
import client.network.ResponseHandler;
import static client.util.NavigationUtils.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateAuctionController {

    private List<File> imageFiles = new ArrayList<>();
    private int currentImageIndex = 0;

    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField txtName, txtStartingBid, txtMinIncrement;
    @FXML private TextArea txtDescription;
    @FXML private VBox imgItems;
    @FXML private ImageView imagePreview;
    @FXML private TextField txtStartHour, txtStartMin, txtEndHour, txtEndMin;
    @FXML private DatePicker dpStartDate, dpEndDate;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (txtName != null && txtName.getScene() != null) {
                Stage stage = (Stage) txtName.getScene().getWindow();
                ResponseHandler.setMainStage(stage);
            }
        });
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        try {
            // --- BƯỚC 1: VALIDATION ---
            if (txtName.getText().isEmpty() || txtStartingBid.getText().isEmpty() ||
                    dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
                showError("Please fill in all information");
                return;
            }

            if (imageFiles.isEmpty()) {
                showError("Please upload at least one image");
                return;
            }

            // --- BƯỚC 2: XỬ LÝ THỜI GIAN ---
            int startH = Integer.parseInt(txtStartHour.getText());
            int startM = Integer.parseInt(txtStartMin.getText());
            LocalDateTime startDateTime = dpStartDate.getValue().atTime(startH, startM);

            int endH = Integer.parseInt(txtEndHour.getText());
            int endM = Integer.parseInt(txtEndMin.getText());
            LocalDateTime endDateTime = dpEndDate.getValue().atTime(endH, endM);

            if (endDateTime.isBefore(startDateTime)) {
                showError("End time must be after start time!");
                return;
            }

            // --- BƯỚC 3: TẠO ĐỐI TƯỢNG DỮ LIỆU ---
            Item item = new Item();
            item.setId("ITM" + UUID.randomUUID().toString().substring(0, 8)); // Tạo ID tránh null
            item.setName(txtName.getText());
            item.setDescription(txtDescription.getText());
            item.setCategory(Category.valueOf(cbCategory.getValue()));

            List<String> imagePaths = new ArrayList<>();
            for (File file : imageFiles) imagePaths.add(file.toURI().toString());
            item.setImages(imagePaths);

            Auction newAuction = new Auction();
            newAuction.setId("AUC" + UUID.randomUUID().toString().substring(0, 8)); // Tạo ID tránh null
            newAuction.setItem(item);
            newAuction.setCurrentPrice(new java.math.BigDecimal(txtStartingBid.getText()));
            newAuction.setMinIncrement(new java.math.BigDecimal(txtMinIncrement.getText()));
            newAuction.setStartTime(startDateTime);
            newAuction.setEndTime(endDateTime);
            newAuction.setStatus(AuctionStatus.ACTIVE);

            // --- BƯỚC 4: GỬI LÊN SERVER ---
            ResponseHandler.setMainStage((Stage) ((Node) event.getSource()).getScene().getWindow());

            ClientSocket socket = ClientSocket.getInstance();
            if (socket == null) {
                showError("Not connected to server."); return;
            }
            socket.sendCreate(newAuction);

        } catch (NumberFormatException e) {
            showError("Price and time must be numeric values");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        switchScene(stage, "/fxml/HomePage.fxml", "Auction Home");
    }

    // --- HÀM XỬ LÝ ẢNH ---
    @FXML
    private void handleUploadImages() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Choose Images");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg"
                )
        );

        List<File> files = fileChooser.showOpenMultipleDialog(imgItems.getScene().getWindow());

        if (files != null && !files.isEmpty()) {

            imageFiles.clear();

            imageFiles.addAll(files);

            currentImageIndex = 0;

            showImage(currentImageIndex);

            imgItems.setVisible(false);

            imagePreview.setOnMouseClicked(e -> handleUploadImages());

        }
    }
    private void showImage(int index) {

        if (imageFiles.isEmpty()) {
            return;
        }

        Image image = new Image(
                imageFiles.get(index).toURI().toString()
        );

        imagePreview.setImage(image);
    }
    @FXML
    private void showPreviousImage() {

        if (imageFiles.isEmpty()) {
            return;
        }

        currentImageIndex--;

        if (currentImageIndex < 0) {
            currentImageIndex = imageFiles.size() - 1;
        }

        showImage(currentImageIndex);
    }
    @FXML
    private void showNextImage() {

        if (imageFiles.isEmpty()) {
            return;
        }

        currentImageIndex++;

        if (currentImageIndex >= imageFiles.size()) {
            currentImageIndex = 0;
        }

        showImage(currentImageIndex);
    }
}
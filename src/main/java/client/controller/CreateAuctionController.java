package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import client.network.response.ResponseHandler;
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

        User currentUser =
                UserSession.getCurrentUser();

        if(currentUser != null){
            currentUser.setRole(Role.SELLER);

            System.out.println(
                    "[ROLE] "
                            + currentUser.getUsername()
                            + " -> SELLER"
            );
        }

        Platform.runLater(() -> {
            if (txtName != null && txtName.getScene() != null) {
                Stage stage =
                        (Stage) txtName.getScene().getWindow();

                ResponseHandler.setMainStage(stage);
            }
        });
        //đổi role nếu force close page (ấn X)
        Platform.runLater(() -> {
            Stage stage =
                    (Stage) txtName.getScene().getWindow();

            stage.addEventHandler(
                    javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST,
                    e -> resetRoleToBidder()
            );
        });
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        try {
            // --- VALIDATION ---
            if (txtName.getText().trim().isEmpty() || txtStartingBid.getText().isEmpty() ||
                    dpStartDate.getValue() == null || dpEndDate.getValue() == null ||
                    cbCategory.getValue() == null) {
                showError("Please fill in all information and select a category!");
                return;
            }

            // --- UPLOAD ẢNH LÊN CLOUDINARY ---
            List<String> cloudinaryUrls = new ArrayList<>();
            if (imageFiles.isEmpty()) {
                showError("Please upload at least one image!");
                return;
            }

            for (File file : imageFiles) {
                String url = client.util.CloudinaryUploader.upload(file);
                if (url != null) {
                    cloudinaryUrls.add(url);
                } else {
                    showError("Failed to upload image: " + file.getName());
                    return;
                }
            }

            // --- 2. DATA CLEANING & TIME ---
            String cleanName = txtName.getText().replace("|", "-").replace(";", ",");
            String cleanDesc = txtDescription.getText().replace("|", "-").replace(";", ",").replace("\n", " ");

            int startH = txtStartHour.getText().isEmpty() ? 0 : Integer.parseInt(txtStartHour.getText());
            int startM = txtStartMin.getText().isEmpty() ? 0 : Integer.parseInt(txtStartMin.getText());
            LocalDateTime startDateTime = dpStartDate.getValue().atTime(startH, startM);

            int endH = txtEndHour.getText().isEmpty() ? 0 : Integer.parseInt(txtEndHour.getText());
            int endM = txtEndMin.getText().isEmpty() ? 0 : Integer.parseInt(txtEndMin.getText());
            LocalDateTime endDateTime = dpEndDate.getValue().atTime(endH, endM);

            // --- 3. MAP ITEM & AUCTION ---
            Item item = new Item();
            item.setItem_id("ITM" + UUID.randomUUID().toString().substring(0, 8));
            item.setName(cleanName);
            item.setDescription(cleanDesc);

            try {
                item.setCategory(Category.valueOf(cbCategory.getValue().toUpperCase()));
            } catch (Exception e) {
                item.setCategory(Category.OTHER);
            }

            item.setImages(cloudinaryUrls);

            Auction newAuction = new Auction();
            newAuction.setAuction_id("AUC" + UUID.randomUUID().toString().substring(0, 8));
            newAuction.setItem(item);
            newAuction.setCurrentPrice(new java.math.BigDecimal(txtStartingBid.getText()));
            newAuction.setMinIncrement(new java.math.BigDecimal(txtMinIncrement.getText()));
            newAuction.setStartTime(startDateTime);
            newAuction.setEndTime(endDateTime);

            // --- 4. SEND TO SERVER ---
            ClientSocket socket = ClientSocket.getInstance();
            if (socket != null) {
                socket.sendCreate(newAuction);

                resetRoleToBidder();

                switchScene(
                        (Stage)((Node)event.getSource()).getScene().getWindow(),
                        "/fxml/HomePage.fxml",
                        "Home Page"
                );
            } else {
                showError("Connection Error: Not connected to server!");
            }

        } catch (NumberFormatException e) {
            showError("Invalid input: Price and time must be numeric values!");
        } catch (Exception e) {
            showError("System Error: " + e.getMessage());
        }
    }


    @FXML
    private void handleCancel(ActionEvent event) {

        resetRoleToBidder();

        User currentUser =
                UserSession.getCurrentUser();

        if(currentUser != null){
            System.out.println(
                    "[ROLE] User "
                            + currentUser.getUsername()
                            + " reverted role to BIDDER"
            );
        }

        Stage stage =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        switchScene(stage,
                "/fxml/HomePage.fxml",
                "Auction Home");
    }

    private void resetRoleToBidder() {
        User currentUser = UserSession.getCurrentUser();

        if (currentUser != null) {
            currentUser.setRole(Role.BIDDER);

            System.out.println(
                    "[ROLE] "
                            + currentUser.getUsername()
                            + " -> BIDDER"
            );
        }
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
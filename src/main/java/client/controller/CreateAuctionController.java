package client.controller;

import client.manager.UserSession;
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
            // --- VALIDATION ---
            if (txtName.getText().trim().isEmpty() || txtStartingBid.getText().isEmpty() ||
                    dpStartDate.getValue() == null || dpEndDate.getValue() == null ||
                    cbCategory.getValue() == null) {
                showError("Please fill in all information and select a category!");
                return;
            }


            // --- DATA CLEANING ---
            // Thay thế các ký tự gây vỡ chuỗi
            String cleanName = txtName.getText().replace("|", "-").replace(";", ",");
            String cleanDesc = txtDescription.getText().replace("|", "-").replace(";", ",").replace("\n", " ");

            // --- TIME PROCESSING ---
            // Dùng mặc định là 00:00 nếu người dùng quên nhập giờ/phút
            int startH = txtStartHour.getText().isEmpty() ? 0 : Integer.parseInt(txtStartHour.getText());
            int startM = txtStartMin.getText().isEmpty() ? 0 : Integer.parseInt(txtStartMin.getText());
            LocalDateTime startDateTime = dpStartDate.getValue().atTime(startH, startM);

            int endH = txtEndHour.getText().isEmpty() ? 0 : Integer.parseInt(txtEndHour.getText());
            int endM = txtEndMin.getText().isEmpty() ? 0 : Integer.parseInt(txtEndMin.getText());
            LocalDateTime endDateTime = dpEndDate.getValue().atTime(endH, endM);

            if (!endDateTime.isAfter(startDateTime)) {
                showError("End time must be after start time!");
                return;
            }
            if (startDateTime.isBefore(LocalDateTime.now())) {
                showError("Start time must be in the future!");
                return;
            }

            // --- MAP ---
            Item item = new Item();
            // Để Server tự sinh ID hoặc dùng UUID ngắn gọn
            item.setItem_id("ITM" + UUID.randomUUID().toString().substring(0, 8));
            item.setName(cleanName);
            item.setDescription(cleanDesc);

            // Đảm bảo Category khớp với Enum (toUpperCase)
            try {
                item.setCategory(Category.valueOf(cbCategory.getValue().toUpperCase()));
            } catch (Exception e) {
                item.setCategory(Category.OTHER);
            }

            List<String> imagePaths = new ArrayList<>();
            // Nếu không có ảnh, ta gửi chuỗi NO_IMAGE (đã xử lý ở ClientSocket)
            for (File file : imageFiles) imagePaths.add(file.toURI().toString());
            item.setImages(imagePaths);

            Auction newAuction = new Auction();
            newAuction.setAuction_id("AUC" + UUID.randomUUID().toString().substring(0, 8));
            newAuction.setItem(item);
            newAuction.setCurrentPrice(new java.math.BigDecimal(txtStartingBid.getText()));
            newAuction.setMinIncrement(new java.math.BigDecimal(txtMinIncrement.getText()));
            newAuction.setStartTime(startDateTime);
            newAuction.setEndTime(endDateTime);

            //check nếu tgian start xa hơn tgian hện tại thì đổi status thành upcoming
            AuctionStatus status =
                    startDateTime.isAfter(LocalDateTime.now())
                            ? AuctionStatus.UPCOMING
                            : AuctionStatus.ACTIVE;
            newAuction.setStatus(status);

            // --- SEND ---
            ClientSocket socket = ClientSocket.getInstance();
            if (socket != null) {
                System.out.println("→ Sending CREATE command for item: " + cleanName);
                socket.sendCreate(newAuction);
                User currentUser =
                        UserSession.getCurrentUser();

                if (currentUser != null) {
                    currentUser.setRole(Role.BIDDER);

                    System.out.println(
                            "[ROLE] User "
                                    + currentUser.getUsername()
                                    + " changed role to BIDDER after creating auction"
                    );
                }

            } else {
                showError("Connection Error: Not connected to server!");
            }

        } catch (NumberFormatException e) {
            showError("Invalid input: Price and time must be numeric values!");
        } catch (Exception e) {
            showError("System Error: " + e.getMessage());
            System.err.println("Error: " + e.getMessage());        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {

        User currentUser =
                UserSession.getCurrentUser();

        if (currentUser != null) {
            currentUser.setRole(Role.BIDDER);
        }
        System.out.println(
                "[ROLE] User "
                        + currentUser.getUsername()
                        + " reverted role to BIDDER (cancel create auction)"
        );

        Stage stage =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        switchScene(stage,
                "/fxml/HomePage.fxml",
                "Auction Home");
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
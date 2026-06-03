package client.controller;

import client.manager.ControllerRegistry;
import client.manager.UserSession;
import client.network.response.ResponseHandler;
import client.util.CloudinaryUploader;
import client.util.NavigationUtils;
import client.util.AlertUtils;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import model.Auction;
import model.Category;
import model.Item;
import model.Role;
import model.User;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserCreateNewAuctionController extends BaseController {

    private static final double IMAGE_RADIUS = 20;
    private final List<File> imageFiles = new ArrayList<>();
    private int currentImageIndex = 0;

    // ==================== FXML ====================
    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField txtName;
    @FXML private TextField txtStartingBid;
    @FXML private TextField txtMinIncrement;
    @FXML private TextArea txtDescription;
    @FXML private VBox imgItems;
    @FXML private ImageView imagePreview;
    @FXML private TextField txtStartHour;
    @FXML private TextField txtStartMin;
    @FXML private TextField txtEndHour;
    @FXML private TextField txtEndMin;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;

    // ==================== INIT ====================
    @FXML
    public void initialize() {
        System.out.println("Create Auction Loaded");
        updateUserRole(Role.SELLER);
        setupStage();
        setupImagePreview();
    }

    private void setupStage() {
        Platform.runLater(() -> {
            if (txtName == null
                    || txtName.getScene() == null) {
                return;
            }
            Stage stage =
                    (Stage) txtName
                            .getScene()
                            .getWindow();
            ResponseHandler.setMainStage(stage);
            stage.setOnCloseRequest(
                    e -> resetRoleToBidder()
            );
        });
    }

    private void setupImagePreview() {
        Rectangle clip = new Rectangle(500, 360);
        clip.setArcWidth(IMAGE_RADIUS * 2);
        clip.setArcHeight(IMAGE_RADIUS * 2);
        imagePreview.setClip(clip);
    }

    // ==================== CREATE ====================
    @FXML
    private void handleCreate() {
        try {
            if (!validateInput()) {
                return;
            }
            List<String> imageUrls =
                    uploadImages();

            if (imageUrls == null) {
                return;
            }

            Auction auction = createAuction(imageUrls);
            client.sendCreate(auction);
            UserHomePageController home = ControllerRegistry.get(UserHomePageController.class);

            if (home != null) {
                home.refreshData();
            }
            resetRoleToBidder();
            navigateHome();

        } catch (NumberFormatException e) {
            AlertUtils.error("Price and time must be numeric values.");

        } catch (Exception e) {
            AlertUtils.error("System Error: " + e.getMessage());
        }
    }

    // ==================== VALIDATION ====================
    private boolean validateInput() {
        if (txtName.getText().trim().isEmpty()
                || txtStartingBid.getText().trim().isEmpty()
                || cbCategory.getValue() == null
                || dpStartDate.getValue() == null
                || dpEndDate.getValue() == null) {

            AlertUtils.error("Please fill in all required fields.");
            return false;
        }

        if (imageFiles.isEmpty()) {
            AlertUtils.error("Please upload at least one image.");
            return false;
        }

        LocalDateTime start = getStartDateTime();
        LocalDateTime end = getEndDateTime();
        if (!start.isAfter(LocalDateTime.now())) {
            AlertUtils.error("Start time must be in the future.");
            return false;
        }

        if (!end.isAfter(start)) {
            AlertUtils.error("End time must be after start time.");
            return false;
        }
        return true;
    }

    // ==================== CREATE AUCTION ====================
    private Auction createAuction(List<String> imageUrls) {
        Item item = createItem(imageUrls);
        Auction auction = new Auction();
        auction.setAuction_id(
                "AUC"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );

        auction.setItem(item);
        auction.setCurrentPrice(
                new BigDecimal(
                        txtStartingBid.getText()
                )
        );
        auction.setMinIncrement(
                new BigDecimal(
                        txtMinIncrement.getText()
                )
        );
        auction.setStartTime(getStartDateTime());
        auction.setEndTime(getEndDateTime());
        return auction;
    }

    private Item createItem(List<String> imageUrls) {
        Item item = new Item();
        item.setItem_id(
                "ITM"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );
        item.setName(sanitize(txtName.getText()));
        item.setDescription(sanitize(txtDescription.getText()));
        item.setCategory(parseCategory());
        item.setImages(imageUrls);
        return item;
    }

    // ==================== IMAGE UPLOAD ====================
    private List<String> uploadImages() {
        List<String> uploadedUrls =
                new ArrayList<>();
        for (File file : imageFiles) {
            String url = CloudinaryUploader.upload(file);
            if (url == null) {
                AlertUtils.error(
                        "Failed to upload image: "
                                + file.getName()
                );
                return null;
            }
            uploadedUrls.add(url);
        }
        return uploadedUrls;
    }

    // ==================== IMAGE ====================
    @FXML
    private void handleUploadImages() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Images");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg"
                )
        );
        List<File> files =
                chooser.showOpenMultipleDialog(
                        imgItems.getScene()
                                .getWindow()
                );

        if (files == null || files.isEmpty()) {
            return;
        }

        imageFiles.clear();
        imageFiles.addAll(files);
        currentImageIndex = 0;
        showImage(currentImageIndex);
        imgItems.setVisible(false);
        imagePreview.setOnMouseClicked(
                e -> handleUploadImages()
        );
    }

    private void showImage(int index) {
        if (imageFiles.isEmpty()) {
            return;
        }
        Image image =
                new Image(
                        imageFiles.get(index)
                                .toURI()
                                .toString()
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
            currentImageIndex =
                    imageFiles.size() - 1;
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

    // ==================== ROLE ====================
    private void updateUserRole(Role role) {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            return;
        }
        user.setRole(role);
        System.out.println(
                "[ROLE] "
                        + user.getUsername()
                        + " -> "
                        + role
        );
    }

    private void resetRoleToBidder() {
        updateUserRole(Role.BIDDER);
    }

    // ==================== UTIL ====================
    private String sanitize(String text) {
        return text
                .replace("|", "-")
                .replace(";", ",")
                .replace("\n", " ");
    }

    private Category parseCategory() {
        try {
            return Category.valueOf(
                    cbCategory.getValue()
                            .toUpperCase()
            );
        } catch (Exception e) {
            return Category.OTHER;
        }
    }

    private LocalDateTime getStartDateTime() {
        int hour =
                txtStartHour.getText().isBlank()
                        ? 0
                        : Integer.parseInt(
                        txtStartHour.getText()
                );

        int minute =
                txtStartMin.getText().isBlank()
                        ? 0
                        : Integer.parseInt(
                        txtStartMin.getText()
                );

        return dpStartDate
                .getValue()
                .atTime(hour, minute);
    }

    private LocalDateTime getEndDateTime() {
        int hour =
                txtEndHour.getText().isBlank()
                        ? 0
                        : Integer.parseInt(
                        txtEndHour.getText()
                );

        int minute =
                txtEndMin.getText().isBlank()
                        ? 0
                        : Integer.parseInt(
                        txtEndMin.getText()
                );

        return dpEndDate
                .getValue()
                .atTime(hour, minute);
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleCancel() {
        resetRoleToBidder();
        navigateHome();
    }

    private void navigateHome() {
        NavigationUtils.switchScene(
                (Stage) txtName
                        .getScene()
                        .getWindow(),
                "/fxml/userHomePage-view.fxml",
                "Home Page"
        );
    }
}


package client.controller;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;

import java.time.LocalDateTime;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import model.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class CreateAuctionController {

    private List<File> imageFiles = new ArrayList<>();
    private int currentImageIndex = 0;
    @FXML
    private ComboBox<String> cbCategory;
    @FXML
    public void initialize() {

        cbCategory.getItems().addAll(
                "ELECTRONICS",
                "FASHION",
                "HOME",
                "BOOK",
                "OTHER"
        );

    }
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtStartingBid;
    @FXML private TextField txtMinIncrement;
    @FXML private HBox imgItem;
    @FXML private ImageView imagePreview;

    // Thành phần thời gian bắt đầu
    @FXML private TextField txtStartHour;
    @FXML private TextField txtStartMin;
    @FXML private DatePicker dpStartDate;

    // Thành phần thời gian kết thúc
    @FXML private TextField txtEndHour;
    @FXML private TextField txtEndMin;
    @FXML private DatePicker dpEndDate;


    @FXML
    private void handleCreate(ActionEvent event) {
        try {
            // --- BƯỚC 1: VALIDATION ---

            if (txtName.getText().isEmpty()
                    || txtDescription.getText().isEmpty()
                    || txtStartingBid.getText().isEmpty()
                    || txtMinIncrement.getText().isEmpty()
                    || txtStartHour.getText().isEmpty()
                    || txtStartMin.getText().isEmpty()
                    || txtEndHour.getText().isEmpty()
                    || txtEndMin.getText().isEmpty()
                    || dpStartDate.getValue() == null
                    || dpEndDate.getValue() == null) {

                showAlert("Error", "Please fill in all information");
                return;

            }
            if (cbCategory.getValue() == null) {
                showAlert("Error", "Please choose category");
                return;
            }


            if (imageFiles.isEmpty()) {

                showAlert("Error", "Please upload at least one image");
                return;
            }

            // --- BƯỚC 2: XỬ LÝ THỜI GIAN ---
            int startH = Integer.parseInt(txtStartHour.getText());
            int startM = Integer.parseInt(txtStartMin.getText());
            LocalDateTime startDateTime = dpStartDate.getValue().atTime(startH, startM);

            int endH = Integer.parseInt(txtEndHour.getText());
            int endM = Integer.parseInt(txtEndMin.getText());

            if (startH < 0 || startH > 23
                    || startM < 0 || startM > 59
                    || endH < 0 || endH > 23
                    || endM < 0 || endM > 59) {

                showAlert("Error", "Invalid time format");
                return;
            }

            LocalDateTime endDateTime = dpEndDate.getValue().atTime(endH, endM);
            if (endDateTime.isBefore(startDateTime)) {
                showAlert("Error", "End time must be after start time!");
                return;
            }

            // --- BƯỚC 3: TẠO ĐỐI TƯỢNG DỮ LIỆU ---

            // 1. Tạo Item chứa thông tin cơ bản
            Item item = new Item();
            item.setName(txtName.getText());
            item.setDescription(txtDescription.getText());
            item.setCategory(
                    Category.valueOf(
                            cbCategory.getValue()
                    )
            );

            item.setImages(
                    imageFiles.get(0)
                            .toURI()
                            .toString()
            );

            // 2. Tạo Auction chứa thông tin đấu giá (Thay thế cho AuctionItem cũ)
            Auction newAuction = new Auction();
            newAuction.setItem(item);
            newAuction.setCurrentPrice(new java.math.BigDecimal(txtStartingBid.getText()));
            newAuction.setMinIncrement(new java.math.BigDecimal(txtMinIncrement.getText()));
            newAuction.setEndTime(endDateTime);
            newAuction.setStatus(AuctionStatus.ACTIVE);

            // --- BƯỚC 4: CHUYỂN TRANG VÀ HIỂN THỊ ---
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-view.fxml"));
            Parent userViewParent = loader.load();

            UserViewController userController = loader.getController();

            // Truyền newAuction
            userController.addNewAuctionCard(newAuction);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(userViewParent));
            stage.show();

        } catch (NumberFormatException e) {
            showAlert("Format Error", "Price, hour, and minutes must be numeric value");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("System Error", "Could not load the display interface");
        }
    }
    @FXML
    private void handleCancel(ActionEvent event) {
        try {
            // 1. Tải file FXML của trang chủ
            // Lưu ý: Thay "HomeView.fxml" bằng tên file thực tế của bạn
            Parent homePage = FXMLLoader.load(getClass().getResource("/fxml/user-view.fxml"));

            // 2. Tạo một Scene mới với trang chủ
            Scene homeScene = new Scene(homePage);

            // 3. Lấy Stage (cửa sổ) hiện tại từ sự kiện nhấn nút
            Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 4. Đặt Scene mới lên Stage và hiển thị
            appStage.setScene(homeScene);
            appStage.show();

        } catch (IOException e) {
            System.out.println("Could not find user-view.fxml");
            e.printStackTrace();
        }
    }
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

        List<File> files = fileChooser.showOpenMultipleDialog(imgItem.getScene().getWindow());

        if (files != null && !files.isEmpty()) {

            imageFiles.clear();

            imageFiles.addAll(files);

            currentImageIndex = 0;

            showImage(currentImageIndex);

            imgItem.setVisible(false);
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

    // Hàm tiện ích để hiện thông báo
    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
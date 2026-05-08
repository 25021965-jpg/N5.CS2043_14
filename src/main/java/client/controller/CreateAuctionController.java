package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.time.LocalDateTime;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import model.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class CreateAuctionController {
    private java.io.File selectedImageFile;

    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtStartingBid;
    @FXML private TextField txtMinIncrement;
    @FXML private HBox imgItem;

    // Thành phần thời gian bắt đầu
    @FXML private TextField txtStartHour;
    @FXML private TextField txtStartMin;
    @FXML private DatePicker dpStartDate;

    // Thành phần thời gian kết thúc
    @FXML private TextField txtEndHour;
    @FXML private TextField txtEndMin;
    @FXML private DatePicker dpEndDate;

    @FXML
    private void handleUploadImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choose item image");

        // Chỉ lọc các định dạng ảnh
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Mở cửa sổ chọn file
        selectedImageFile = fileChooser.showOpenDialog(imgItem.getScene().getWindow());

        if (selectedImageFile != null) {
            // Cập nhật giao diện để người dùng biết đã chọn ảnh thành công
            // Ví dụ: Đổi màu viền hoặc đổi chữ trong HBox
            imgItem.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2;");

            // Tìm cái Label đầu tiên trong HBox để đổi text thành tên file
            javafx.scene.control.Label lbl = (javafx.scene.control.Label) imgItem.getChildren().get(0);
            lbl.setText("Selected: " + selectedImageFile.getName());
        }
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        try {
            // --- BƯỚC 1: VALIDATION ---
            if (txtName.getText().isEmpty() || dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
                showAlert("Error", "Please fill in all information");
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
                showAlert("Error", "End time must be after start time!");
                return;
            }

            // --- BƯỚC 3: TẠO ĐỐI TƯỢNG DỮ LIỆU ---

            // 1. Tạo Item chứa thông tin cơ bản
            Item item = new Item();
            item.setName(txtName.getText());
            item.setDescription(txtDescription.getText());
            if (selectedImageFile != null) {
                item.setImages(String.valueOf(java.util.Collections.singletonList(selectedImageFile.toURI().toString())));
            }

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

    // Hàm tiện ích để hiện thông báo
    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
package client.controller;

import client.network.ClientSocket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField userField;
    @FXML private PasswordField passField;

    private ClientSocket client;

    @FXML
    public void initialize() {
        System.out.println("=== 1. initialize() START ===");

        try {
            System.out.println("=== 2. Đang tạo ClientSocket... ===");
            client = new ClientSocket();
            System.out.println("=== 3. ClientSocket tạo thành công ===");

            System.out.println("=== 4. Đang lắng nghe server... ===");
            client.listen(msg -> {
                System.out.println("=== 5. Nhận từ server: " + msg + " ===");

                Platform.runLater(() -> {
                    if (msg.startsWith("LOGIN_SUCCESS")) {
                        System.out.println("=== 6. ĐĂNG NHẬP THÀNH CÔNG ===");
                        showAlert("Success", "Login successful!");
                        goToAuction();
                    }
                    else if (msg.startsWith("LOGIN_FAILED")) {
                        System.out.println("=== 6. ĐĂNG NHẬP THẤT BẠI ===");
                        showAlert("Error", "Wrong username or password!");
                        passField.clear();
                        passField.requestFocus();
                    }
                    else if (msg.startsWith("REGISTER_SUCCESS")) {
                        System.out.println("=== 6. ĐĂNG KÝ THÀNH CÔNG ===");
                        showAlert("Success", "Register successful! Please login.");
                    }
                    else if (msg.startsWith("REGISTER_FAILED")) {
                        System.out.println("=== 6. ĐĂNG KÝ THẤT BẠI ===");
                        showAlert("Error", "Username or email already exists!");
                    }
                });
            });

            System.out.println("=== 7. Hiện alert kết nối thành công ===");
            showAlert("Success", "Connected to server");

        } catch (Exception e) {
            System.out.println("=== LỖI trong initialize(): " + e.getMessage() + " ===");
            e.printStackTrace();
            showAlert("Error", "Cannot connect to server: " + e.getMessage());
        }

        System.out.println("=== 8. initialize() END ===");
    }

    @FXML
    private void handleLogin() {
        System.out.println("=== handleLogin() START ===");

        String username = userField.getText().trim();
        String password = passField.getText().trim();

        System.out.println("=== Username: " + username + ", Password: " + password + " ===");

        if (username.isEmpty()) {
            System.out.println("=== Username rỗng ===");
            showAlert("Error", "Enter username!");
            userField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            System.out.println("=== Password rỗng ===");
            showAlert("Error", "Enter password!");
            passField.requestFocus();
            return;
        }

        System.out.println("=== Gửi yêu cầu LOGIN lên server ===");
        client.sendLogin(username, password);
        System.out.println("=== Đã gửi, chờ phản hồi... ===");
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        System.out.println("=== goToRegister() START ===");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/register-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            System.out.println("=== Chuyển sang màn hình đăng ký thành công ===");
        } catch (Exception e) {
            System.out.println("=== LỖI goToRegister(): " + e.getMessage() + " ===");
            e.printStackTrace();
            showAlert("Error", "Cannot open register screen!");
        }
    }

    private void goToAuction() {
        System.out.println("=== goToAuction() START ===");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/user-view.fxml"));
            Stage stage = (Stage) userField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Auction");
            System.out.println("=== Chuyển sang màn hình auction thành công ===");
        } catch (Exception e) {
            System.out.println("=== LỖI goToAuction(): " + e.getMessage() + " ===");
            e.printStackTrace();
            showAlert("Error", "Cannot open auction screen!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
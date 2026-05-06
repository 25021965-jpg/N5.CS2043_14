package client.controller;

import client.network.ClientSocket;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField verifyPasswordField;
    @FXML private DatePicker dobField;

    private ClientSocket client;

    public void setClient(ClientSocket client) {
        this.client = client;
    }

    public void startListening() {
        if (client != null) {
            client.listen(msg -> {
                System.out.println("Server: " + msg);
                Platform.runLater(() -> handleResponse(msg));
            });
        }
    }

    @FXML
    public void initialize() {
        // Không làm gì ở đây
    }

    @FXML
    private void handleCreate() {
        if (client == null) {
            showAlert("Error", "Not connected to server");
            return;
        }

        String fullname = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String verify = verifyPasswordField.getText();

        if (fullname.isEmpty()) {
            showAlert("Error", "Enter full name!");
            fullNameField.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            showAlert("Error", "Enter username!");
            usernameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            showAlert("Error", "Enter email!");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showAlert("Error", "Enter password!");
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters!");
            passwordField.requestFocus();
            return;
        }

        if (!password.equals(verify)) {
            showAlert("Error", "Passwords do not match!");
            verifyPasswordField.clear();
            verifyPasswordField.requestFocus();
            return;
        }

        client.sendRegister(fullname, username, email, password);
        showAlert("Info", "Registering...");
    }

    private void handleResponse(String msg) {
        System.out.println("=== handleResponse: " + msg + " ===");

        if (msg.startsWith("REGISTER_SUCCESS")) {
            System.out.println("=== ĐĂNG KÝ THÀNH CÔNG, CHUYỂN VỀ LOGIN ===");
            // Chuyển về màn hình login
            Platform.runLater(() -> {
                goToLogin();
            });
        }
        else if (msg.startsWith("REGISTER_FAILED")) {
            showAlert("Error", "Username or email already exists!");
        }
        else if (msg.startsWith("ERROR")) {
            showAlert("Error", msg);
        }
        else if (msg.startsWith("DISCONNECTED")) {
            showAlert("Error", "Disconnected from server!");
        }
    }

    private void goToLogin() {
        System.out.println("=== goToLogin() START ===");
        try {
            System.out.println("=== Loading login-view.fxml... ===");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Parent root = loader.load();
            System.out.println("=== Load FXML success ===");

            LoginController controller = loader.getController();
            controller.setClient(client);
            controller.startListening();

            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            System.out.println("=== Chuyển về màn hình Login thành công ===");

        } catch (Exception e) {
            System.out.println("=== LỖI goToLogin(): " + e.getMessage() == "===");
            e.printStackTrace();
            showAlert("Error", "Cannot open login screen: " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        System.out.println("=== goToLogin(ActionEvent) START ===");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setClient(client);
            controller.startListening();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");

        } catch (Exception e) {
            System.out.println("=== LỖI goToLogin(ActionEvent): " + e.getMessage() + " ===");
            e.printStackTrace();
            showAlert("Error", "Cannot open login screen!");
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
package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private DatePicker dobField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField verifyPasswordField;

    @FXML
    private void handleCreate() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String verify = verifyPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill all required fields");
            return;
        }

        if (!password.equals(verify)) {
            showAlert("Error", "Passwords do not match");
            return;
        }

        // giả lập đăng ký thành công
        showAlert("Success", "Account created successfully!");

        // quay lại login
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login-view.fxml")
            );
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
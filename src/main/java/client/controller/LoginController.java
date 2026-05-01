package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passField;

    @FXML
    public void initialize() {
        // Runs when the view is loaded
    }

    @FXML
    private void handleLogin() {

        String username = userField.getText().trim();
        String password = passField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter your ID and password.");
            return;
        }

        // Demo login
        if (username.equals("admin") && password.equals("123")) {
            showAlert("Success", "Login successful!");
        } else {
            showAlert("Login Failed", "Invalid ID or password.");
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
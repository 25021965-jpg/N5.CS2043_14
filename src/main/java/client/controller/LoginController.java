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
    @FXML
    private void goToRegister(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/register-view.fxml"));

            javafx.scene.Parent root = loader.load();

            // Lấy stage hiện tại
            javafx.stage.Stage stage = (javafx.stage.Stage)
                    ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Register");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
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
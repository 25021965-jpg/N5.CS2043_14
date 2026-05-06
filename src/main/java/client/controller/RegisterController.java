
package client.controller;

import client.network.ClientSocket;
import common.ResponseType;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private DatePicker dobField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField verifyPasswordField;

    private ClientSocket client;

    public void setClient(ClientSocket client) {
        this.client = client;

        client.setHandler(msg -> {
            Platform.runLater(() -> handleResponse(msg));
        });
        
        client.listen();
    }

    @FXML
    private void handleCreate() {

        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String verify = verifyPasswordField.getText();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill all required fields");
            return;
        }

        if (!password.equals(verify)) {
            showAlert("Error", "Passwords do not match");
            return;
        }

        // gửi lên server
        client.sendRegister(username, email, password, "USER");

        showAlert("Info", "Creating account...");
    }

    private void handleResponse(String msg) {

        ResponseType type = ResponseType.from(msg);
        if (type == null) return;

        switch (type) {

            case REGISTER_SUCCESS:
                showAlert("Success", "Account created successfully!");
                goToLogin();
                break;

            case REGISTER_FAILED:
                showAlert("Error", "Username or email already exists!");
                break;

            case ERROR:
                showAlert("Error", msg);
                break;

            case DISCONNECTED:
                showAlert("Error", "Disconnected from server!");
                break;
        }
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login-view.fxml")
            );

            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");

        } catch (Exception e) {
            showAlert("Error", "Cannot open login screen!");
            e.printStackTrace();
        }
    }

    @FXML
    private void backToLogin(ActionEvent event) {
        goToLogin();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

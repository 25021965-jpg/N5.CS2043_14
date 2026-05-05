```java
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

public class LoginController {

    @FXML private TextField userField;
    @FXML private PasswordField passField;

    private ClientSocket client;

    // nhận client từ màn trước
    public void setClient(ClientSocket client) {
        this.client = client;

        // listen tại đây để chắc chắn client != null
        client.listen(msg -> {
            System.out.println("Server: " + msg);
            Platform.runLater(() -> handleResponse(msg));
        });
    }

    @FXML
    public void initialize() {
        // không tạo socket ở đây nữa
    }

    @FXML
    private void handleLogin() {

        if (client == null) {
            showAlert("Error", "Not connected to server");
            return;
        }

        String username = userField.getText().trim();
        String password = passField.getText().trim();

        if (username.isEmpty()) {
            showAlert("Error", "Enter username!");
            userField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showAlert("Error", "Enter password!");
            passField.requestFocus();
            return;
        }

        client.sendLogin(username, password);
        showAlert("Info", "Logging in...");
    }

    private void handleResponse(String msg) {

        ResponseType type = ResponseType.from(msg);
        if (type == null) return;

        switch (type) {

            case LOGIN_SUCCESS:
                showAlert("Success", "Login success!");
                goToAuction();
                break;

            case LOGIN_FAILED:
                showAlert("Error", "Wrong username or password!");
                passField.clear();
                passField.requestFocus();
                break;

            case ERROR:
                showAlert("Error", msg);
                break;

            case DISCONNECTED:
                showAlert("Error", "Disconnected from server!");
                break;
        }
    }

    private void goToAuction() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/user-view.fxml")
            );

            Parent root = loader.load();

            UserController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) userField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Auction");

        } catch (Exception e) {
            showAlert("Error", "Cannot open auction screen!");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/register-view.fxml")
            );

            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");

        } catch (Exception e) {
            showAlert("Error", "Cannot open register screen!");
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
```

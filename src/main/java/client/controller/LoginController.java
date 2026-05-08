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

        System.out.println(
                "=== LoginController initialize() START ==="
        );

        try {

            client = new ClientSocket();

            System.out.println(
                    "=== ClientSocket created ==="
            );

            client.listen(msg -> {

                System.out.println(
                        "=== Received: " + msg + " ==="
                );

                Platform.runLater(() ->
                        handleResponse(msg)
                );
            });

            showAlert(
                    "Success",
                    "Connected to server"
            );

        } catch (Exception e) {

            System.out.println(
                    "=== ERROR: " + e.getMessage() + " ==="
            );

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Cannot connect to server: "
                            + e.getMessage()
            );
        }

        System.out.println(
                "=== LoginController initialize() END ==="
        );
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
    }

    private void handleResponse(String msg) {
        if (msg.startsWith("LOGIN_SUCCESS")) {
            showAlert("Success", "Login successful!");
            goToAuction();
        }
        else if (msg.startsWith("LOGIN_FAILED")) {
            showAlert("Error", "Wrong username or password!");
            passField.clear();
            passField.requestFocus();
        }
        else if (msg.startsWith("REGISTER_SUCCESS")) {
            showAlert("Success", "Register successful! Please login.");
        }
        else if (msg.startsWith("REGISTER_FAILED")) {
            showAlert("Error", "Username or email already exists!");
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register-view.fxml"));
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setClient(client);
            //controller.startListening();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Cannot open register screen!");
        }
    }

    private void goToAuction() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("src/main/resources/fxml/user-view.fxml"));
            Stage stage = (Stage) userField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Auction");
        } catch (Exception e) {
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
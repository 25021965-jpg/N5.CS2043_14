package client.controller;

import client.network.ClientSocket;

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

    @FXML
    public void initialize() {
        try {
            client = new ClientSocket();

            // lắng nghe server
            client.listen(msg -> {
                System.out.println("Server: " + msg );
            });

            showAlert("Success","Connected to server");
        } catch (Exception e) {
            showAlert("Error","Cannot connect to server!");
        }
    }

    @FXML
    private void handleLogin() {

        String username = userField.getText().trim();
        String password = passField.getText().trim();

        if (username.isEmpty()) {
            showAlert("Error","Enter username!");
            userField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showAlert("Error","Enter password!");
            passField.requestFocus();
            return;
        }

        // gửi lên server
        client.sendLogin(username, password);
        showAlert("Success","Logging in...");
    }

    private void handleResponse(String msg) {

        if (msg.startsWith("LOGIN_SUCCESS")) {

            showAlert("Success","Login success!");

            // chuyển sang màn auction
            goToAuction();

        } else if (msg.startsWith("LOGIN_FAILED")) {

            showAlert("Error","Wrong username or password!");
            passField.clear();
            passField.requestFocus();

        } else if (msg.startsWith("ERROR")) {
            showAlert("Error",msg);
        }
    }

    private void goToAuction() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/user-view.fxml"));

            Stage stage = (Stage) userField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Auction");

        } catch (Exception e) {
            showAlert("Error","Cannot open auction screen!");
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/register-view.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");

        } catch (Exception e) {
            showAlert("Error","Cannot open register screen!");
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
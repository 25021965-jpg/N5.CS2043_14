package client.controller;

import client.network.ClientSocket;
import client.network.ResponseHandler;
import static client.util.NavigationUtils.*; // Sử dụng các hàm showInfo, showError

import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField verifyPasswordField;
    @FXML
    private DatePicker dobField;

    private ClientSocket client;

    public void setClient(ClientSocket client) {
        this.client = client;
    }

    @FXML
    public void initialize() {

        if (client == null) {
            client = ClientSocket.getInstance();
        }

        Platform.runLater(() -> {
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            ResponseHandler.setMainStage(stage);
        });
    }

    @FXML
    private void handleCreate() {
        if (client == null) {
            showError("Not connected to server");
            return;
        }

        String fullname = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String verify = verifyPasswordField.getText();

        if (fullname.isEmpty()) {
            showError("Enter full name!");
            fullNameField.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            showError("Enter username!");
            usernameField.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            showError("Enter email!");
            emailField.requestFocus();
            return;
        }
        if (dobField.getValue() == null) {
            showError("Choose date of birth!");
            return;
        }
        if (password.isEmpty()) {
            showError("Enter password!");
            passwordField.requestFocus();
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters!");
            return;
        }

        if (!password.equals(verify)) {
            showError("Passwords do not match!");
            verifyPasswordField.clear();
            verifyPasswordField.requestFocus();
            return;
        }

        String dob = dobField.getValue().toString();

        client.sendRegister(fullname, username, email, password, dob);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            NavigationUtils.switchScene(stage, "/fxml/login-view.fxml", "Login");

            System.out.println("Back to Login");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());        }
    }
}
package client.controller;

import client.network.ClientSocket;
import client.network.response.ResponseHandler;

import client.util.AlertUtils;
import client.util.NavigationUtils;
import client.util.TextUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Period;
import java.util.logging.Logger;

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

    private static final Logger LOGGER =
            Logger.getLogger(RegisterController.class.getName());

    @FXML
    public void initialize() {
        System.out.println("Register Loaded");
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
            AlertUtils.error("Not connected to server");
            return;
        }

        String fullname = TextUtils.toTitleCase(fullNameField.getText());
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String verify = verifyPasswordField.getText();

        if (fullname.isEmpty()) {
            AlertUtils.error("Enter full name!");
            fullNameField.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            AlertUtils.error("Enter username!");
            usernameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            AlertUtils.error("Enter email!");
            emailField.requestFocus();
            return;
        }

        if (dobField.getValue() == null) {
            AlertUtils.error("Choose date of birth!");
            return;
        }

        if (password.isEmpty()) {
            AlertUtils.error("Enter password!");
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 6) {
            AlertUtils.error("Password must be at least 6 characters!");
            return;
        }

        if (!password.equals(verify)) {
            AlertUtils.error("Passwords do not match!");
            verifyPasswordField.clear();
            verifyPasswordField.requestFocus();
            return;
        }
        LocalDate dob = dobField.getValue();
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < 18) {
            AlertUtils.error("You must be at least 18 years old to register!");
            return;
        }
        String dobStr = dobField.getValue().toString();
        client.sendRegister(fullname, username, email, password, dobStr);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NavigationUtils.switchScene(stage, "/fxml/login-view.fxml", "Login");
            System.out.println("Back to Login");
        } catch (Exception e) {
            LOGGER.severe("Error: " + e.getMessage());        }
    }
}
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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {

    @FXML private TextField userField;
    @FXML private PasswordField passField;

    private ClientSocket client;

    private static final Logger LOGGER =
            Logger.getLogger(LoginController.class.getName());

    public void setClient(ClientSocket client) {
        this.client = client;
    }

    @FXML
    public void initialize() {
        LOGGER.info("LoginController initialize() START");

        try {
            client = new ClientSocket();

            LOGGER.info("ClientSocket created");

            client.listen(msg -> {
                LOGGER.fine("Received: " + msg);
                Platform.runLater(() -> handleResponse(msg));
            });

            showInfo("Connected to server");

        } catch (Exception e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Failed to connect to server",
                    e
            );

            showError(
                    "Cannot connect to server: "
                            + e.getMessage()
            );
        }

        LOGGER.info("LoginController initialize() END");
    }

    @FXML
    private void handleLogin() {

        if (client == null) {
            showError("Not connected to server");
            return;
        }

        String username = userField.getText().trim();
        String password = passField.getText();

        if (username.isEmpty()) {
            showError("Enter username!");
            userField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Enter password!");
            passField.requestFocus();
            return;
        }

        client.sendLogin(username, password);
    }

    private void handleResponse(String msg) {

        if (msg.startsWith("LOGIN_SUCCESS")) {
            showInfo("Login successful!");
            goToAuction();

        } else if (msg.startsWith("LOGIN_FAILED")) {
            showError("Wrong username or password!");
            passField.clear();
            passField.requestFocus();

        } else if (msg.startsWith("REGISTER_SUCCESS")) {
            showInfo("Register successful! Please login.");

        } else if (msg.startsWith("REGISTER_FAILED")) {
            showError("Username or email already exists!");
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(
                            getClass().getResource(
                                    "/fxml/register-view.fxml"
                            )
                    )
            );

            Parent root = loader.load();

            RegisterController controller =
                    loader.getController();

            controller.setClient(client);

            Stage stage = (Stage)
                    ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Register");

        } catch (Exception e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Cannot open register screen",
                    e
            );
            showError("Cannot open register screen!");
        }
    }

    private void goToAuction() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(
                            getClass().getResource(
                                    "/fxml/user-view.fxml"
                            )
                    )
            );

            Parent root = loader.load();

            Stage stage = new Stage();

            stage.setScene(new Scene(root));
            stage.setTitle("Auction");

            stage.show();

            Stage currentStage =
                    (Stage) userField
                            .getScene()
                            .getWindow();

            currentStage.close();

        } catch (Exception e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Cannot open auction screen",
                    e
            );

            showError("Cannot open auction screen!");
        }
    }

    private void showInfo(String message) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void showError(String message) {

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
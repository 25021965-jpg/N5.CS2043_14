package client.controller;

import client.network.ClientSocket;
import client.network.ResponseHandler; // Mới
import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.logging.Level;
import java.util.logging.Logger;

import static client.util.NavigationUtils.*;

public class LoginController {

    @FXML private TextField userField;
    @FXML private PasswordField passField;

    private ClientSocket client;
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    public void setClient(ClientSocket client) {
        this.client = client;
    }

    @FXML
    public void initialize() {
        LOGGER.info("LoginController initialize() START");
        try {
            // Nếu chưa có client thì mới tạo mới
            if (client == null) {
                client = ClientSocket.getInstance();
            }

            LOGGER.info("ClientSocket created");

            client.listen();

            // Cần gán Stage cho ResponseHandler ngay khi giao diện sẵn sàng
            Platform.runLater(() -> {
                if (userField.getScene() != null) {
                    Stage stage = (Stage) userField.getScene().getWindow();
                    ResponseHandler.setMainStage(stage);
                }
            });

            LOGGER.info("Connected to server successfully");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to server", e);
            showError("Cannot connect to server: " + e.getMessage());
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

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ResponseHandler.setMainStage(stage);
            switchScene(stage, "/fxml/register-view.fxml", "Register");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot open register screen", e);
            showError("Cannot open register screen!");

        }
    }
    @FXML
    private void goforgotPass(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ResponseHandler.setMainStage(stage);
            switchScene(stage, "/fxml/forgotPass-view.fxml", "Forgot Password");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot open forgot pass screen", e);
            showError("Cannot open forgot password screen!");

        }
    }
}
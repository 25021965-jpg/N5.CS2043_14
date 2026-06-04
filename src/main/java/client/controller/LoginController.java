package client.controller;

import client.network.ClientSocket;
import client.util.AlertUtils;
import client.util.NavigationUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController extends BaseController {

    private static final Logger LOGGER =
            Logger.getLogger(LoginController.class.getName());

    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Button loginBtn;
    @FXML private Hyperlink forgotPasswordLink;

    @FXML
    private void underlineLink() {
        forgotPasswordLink.setUnderline(true);
    }

    @FXML
    private void removeUnderlineLink() {
        forgotPasswordLink.setUnderline(false);
    }

    private final ClientSocket client = ClientSocket.getInstance();

    // ==================== INITIALIZE ====================
    @FXML
    public void initialize() {
        System.out.println("LoginController initialized");
        try {
            if (client == null) {
                AlertUtils.error("Cannot connect to server.");
                return;
            }
            client.listen();

            passField.setOnAction(e -> handleLogin());
            loginBtn.setOnMouseEntered(e ->
                    loginBtn.setStyle("""
                            -fx-background-color: #c8a432;
                            -fx-text-fill: black;
                            -fx-background-radius: 8;
                            -fx-cursor: hand;
                            -fx-padding: 10;
                            """)
            );

            loginBtn.setOnMouseExited(e ->
                    loginBtn.setStyle("""
                            -fx-background-color: #D4AF37;
                            -fx-text-fill: black;
                            -fx-background-radius: 8;
                            -fx-cursor: hand;
                            -fx-padding: 10;
                            """)
            );

            loginBtn.setOnMousePressed(e ->
                    loginBtn.setStyle("""
                            -fx-background-color: #b8931f;
                            -fx-text-fill: black;
                            -fx-background-radius: 8;
                            -fx-cursor: hand;
                            -fx-padding: 10;
                            """)
            );

            loginBtn.setOnMouseReleased(e ->
                    loginBtn.setStyle("""
                            -fx-background-color: #c8a432;
                            -fx-text-fill: black;
                            -fx-background-radius: 8;
                            -fx-cursor: hand;
                            -fx-padding: 10;
                            """)
            );
            Platform.runLater(() -> {
                Stage stage = getStage(userField);
                if (stage != null) {
                    NavigationUtils.setMainStage(stage);
                }
            });

        } catch (Exception e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Login initialize failed",
                    e
            );

            AlertUtils.error(
                    "Cannot connect to server: "
                            + e.getMessage()
            );
        }
    }

    // ==================== LOGIN ====================
    @FXML
    private void handleLogin() {
        loginBtn.setStyle("""
                -fx-background-color: #b8931f;
                -fx-text-fill: black;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                -fx-padding: 10;
                """);

        if (client == null) {
            AlertUtils.error("Server not connected.");
            return;
        }

        String username = safeTrim(userField.getText());
        String password = safeTrim(passField.getText());

        if (username.isBlank()) {
            AlertUtils.error("Please enter username.");
            userField.requestFocus();
            return;
        }

        if (password.isBlank()) {
            AlertUtils.error("Please enter password.");
            passField.requestFocus();
            return;
        }
        client.sendLogin(username, password);
    }

    public void onLoginFailed() {
        loginBtn.setStyle("""
        -fx-background-color: #D4AF37;
        -fx-text-fill: black;
        -fx-background-radius: 8;
        -fx-cursor: hand;
        -fx-padding: 10;
        """);

        passField.clear();
        passField.requestFocus();
    }
    // ==================== NAVIGATION ====================

    @FXML
    private void goToRegister(ActionEvent event) {
        switchScene(
                event,
                "/fxml/register-view.fxml",
                "Register"
        );
    }

    @FXML
    private void goToForgotPass(ActionEvent event) {
        switchScene(
                event,
                "/fxml/forgotPass-view.fxml",
                "Forgot Password"
        );
    }
}


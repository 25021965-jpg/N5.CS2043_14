package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import client.util.NavigationUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import model.Role;
import model.User;

import java.math.BigDecimal;
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

    private final ClientSocket client =
            ClientSocket.getInstance();

    // ==================== INITIALIZE ====================

    @FXML
    public void initialize() {

        LOGGER.info("LoginController initialized");

        try {

            if (client == null) {
                showError("Cannot connect to server.");
                return;
            }

            client.setMessageListener(this::handleServerMessage);

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

            showError(
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
            showError("Server not connected.");
            return;
        }

        String username =
                safeTrim(userField.getText());

        String password =
                safeTrim(passField.getText());

        if (username.isBlank()) {
            showError("Please enter username.");
            userField.requestFocus();
            return;
        }

        if (password.isBlank()) {
            showError("Please enter password.");
            passField.requestFocus();
            return;
        }

        client.sendLogin(username, password);
    }

    // ==================== SERVER RESPONSE ====================

    private void handleServerMessage(String msg) {

        Platform.runLater(() -> {

            LOGGER.info(
                    "[LoginController] Received: " + msg
            );

            if (msg.startsWith("LOGIN_SUCCESS")) {

                User user =
                        parseLoginUser(msg);

                if (user == null) {
                    showError("Failed to parse user data.");
                    return;
                }

                UserSession.setCurrentUser(user);

                if (user.getRole() == Role.ADMIN) {
                    openAdminPage();
                } else {
                    openHomePage();
                }

                return;
            }

            if (msg.startsWith("LOGIN_FAILED")) {

                loginBtn.setStyle("""
        -fx-background-color: #D4AF37;
        -fx-text-fill: black;
        -fx-background-radius: 8;
        -fx-cursor: hand;
        -fx-padding: 10;
        """);

                showError("Wrong username or password.");

                passField.clear();
                passField.requestFocus();
            }
        });
    }

    // ==================== PARSE USER ====================

    private User parseLoginUser(String msg) {

        try {

            String[] parts =
                    msg.split("\\|");

            if (parts.length < 5) {
                return null;
            }

            User user = new User();

            user.setUser_id(parts[1]);
            user.setFullname(parts[2]);
            user.setUsername(parts[3]);
            user.setEmail(parts[4]);

            if (parts.length >= 6) {
                user.setDob(parts[5]);
            }

            if (parts.length >= 7) {

                try {

                    user.setRole(
                            Role.valueOf(
                                    parts[6]
                                            .trim()
                                            .toUpperCase()
                            )
                    );

                } catch (Exception e) {
                    user.setRole(Role.BIDDER);
                }
            }

            if (parts.length >= 8) {

                try {

                    user.setBalance(
                            new BigDecimal(parts[7])
                    );

                } catch (Exception e) {
                    user.setBalance(BigDecimal.ZERO);
                }
            }

            user.setPassword(passField.getText());

            return user;

        } catch (Exception e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Parse login user failed",
                    e
            );

            return null;
        }
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
    private void goforgotPass(ActionEvent event) {

        switchScene(
                event,
                "/fxml/forgotPass-view.fxml",
                "Forgot Password"
        );
    }

    private void openHomePage() {
        NavigationUtils.switchScene(
                getStage(userField),
                "/fxml/HomePage.fxml",
                "Auction System",
                client,
                UserSession.getCurrentUser()
        );
    }

    private void openAdminPage() {
        NavigationUtils.switchScene(
                getStage(userField),
                "/fxml/admin-view.fxml",
                "Admin Dashboard",
                client,
                UserSession.getCurrentUser()
        );
    }
}


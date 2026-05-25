package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import client.network.response.ResponseHandler;
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
import model.Role;
import model.User;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import static client.util.NavigationUtils.switchScene;

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
            if (client == null) {
                client = ClientSocket.getInstance();
            }
            LOGGER.info("ClientSocket created");

            client.setMessageListener(this::handleServerMessage);

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

    // ==================== XỬ LÝ PHẢN HỒI TỪ SERVER ====================
    private void handleServerMessage(String msg) {
        Platform.runLater(() -> {
            System.out.println("[LoginController] Received: " + msg);

            if (msg.startsWith("LOGIN_SUCCESS")) {
                // server gửi: LOGIN_SUCCESS|userId|fullname|username|email|dob|role|balance
                String[] parts = msg.split("\\|");
                User user = null;
                if (parts.length >= 5) {
                    user = new User();
                    user.setUser_id(parts[1]);
                    user.setFullname(parts[2]);
                    user.setUsername(parts[3]);
                    user.setEmail(parts[4]);

                    if (parts.length >= 6) user.setDob(parts[5]);

                    // ← FIX: parts[6] = role
                    if (parts.length >= 7) {
                        try {
                            user.setRole(Role.valueOf(parts[6].trim().toUpperCase()));
                        } catch (Exception e) {
                            user.setRole(Role.BIDDER);
                        }
                    }

                    if (parts.length >= 8) {
                        try {
                            user.setBalance(new BigDecimal(parts[7]));
                        } catch (Exception e) {
                            user.setBalance(BigDecimal.ZERO);
                        }
                    }
                    user.setPassword(passField.getText());
                }

                if (user != null) {
                    UserSession.setCurrentUser(user);
                }

                // ← FIX: check role trước khi navigate
                if (user != null && user.getRole() == Role.ADMIN) {
                    goToAdminPage();
                } else {
                    goToHomePage();
                }

            } else if (msg.startsWith("LOGIN_FAILED")) {
                showError("Wrong username or password!");
                passField.clear();
                passField.requestFocus();
            }
        });
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    // ==================== CHUYỂN SANG HOMEPAGE ====================
    private void goToHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) userField.getScene().getWindow();
            ResponseHandler.setMainStage(currentStage);

            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Auction System");
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open homepage!");
        }
    }

    // ==================== CHUYỂN SANG ADMIN PAGE ====================
    private void goToAdminPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-view.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) userField.getScene().getWindow();
            ResponseHandler.setMainStage(currentStage);

            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Admin Dashboard");
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open admin page!");
        }
    }
}
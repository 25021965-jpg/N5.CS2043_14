package client.controller;

import client.network.ClientSocket;
import client.network.ResponseHandler;
import client.util.NavigationUtils;
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
    private User loggedInUser;
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

            // ========== QUAN TRỌNG: ĐĂNG KÝ LISTENER ==========
            client.setMessageListener(ResponseHandler::handle);
            client.listen();

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

    private void goToHomePage() {
        try {
            // Lấy stage từ userField
            Stage stage = (Stage) userField.getScene().getWindow();
            if (stage == null) {
                System.err.println("Stage is null, cannot navigate to HomePage");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();

            HomePageController controller = loader.getController();
            controller.setClient(client);
            controller.setUser(loggedInUser);

            stage.setScene(new Scene(root));
            stage.setTitle("Auction System");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open homepage!");
        }
    }
}
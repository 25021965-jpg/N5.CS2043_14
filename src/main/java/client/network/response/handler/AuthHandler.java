package client.network.response.handler;

import client.controller.HomePageController;
import client.manager.UserSession;
import client.network.response.parser.UserParser;
import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.stage.Stage;
import model.Role;
import model.User;

public class AuthHandler {

    public static void loginSuccess(String data, Stage stage) {

        User user = UserParser.parse(data);
        UserSession.setCurrentUser(user);

        if (user == null) return;

        NavigationUtils.showToast(stage, "Welcome " + user.getFullname() + "!");

        if (user.getRole() == Role.ADMIN) {
            NavigationUtils.switchScene(stage, "/fxml/admin-view.fxml", "Admin Dashboard");
        } else {
            NavigationUtils.switchScene(stage, "/fxml/HomePage.fxml", "Home");

            Platform.runLater(() -> {
                HomePageController ctrl = HomePageController.getInstance();
                if (ctrl != null) ctrl.setUser(user);
            });
        }
    }

    public static void loginFailed(String data, Stage stage) {
        NavigationUtils.showError("Login failed: " + data);
    }

    public static void registerSuccess(Stage stage) {
        NavigationUtils.switchScene(stage, "/fxml/login-view.fxml", "Login");
        NavigationUtils.showToast(stage, "Account created successfully!");
    }

    public static void registerFailed(String data, Stage stage) {
        NavigationUtils.showError("Register failed: " + data);
    }

    public static void forgotSuccess(Stage stage) {
        NavigationUtils.switchScene(stage, "/fxml/login-view.fxml", "Login");
        NavigationUtils.showToast(stage, "Password reset successfully!");
    }

    public static void forgotFailed(String data, Stage stage) {
        NavigationUtils.showError("Reset failed: " + data);
    }
}
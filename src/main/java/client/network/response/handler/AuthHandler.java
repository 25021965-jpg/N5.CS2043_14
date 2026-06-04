package client.network.response.handler;

import client.controller.UserHomePageController;
import client.controller.LoginController;
import client.manager.ControllerRegistry;
import client.manager.UserSession;
import client.network.ClientSocket;
import client.network.response.parser.UserParser;
import client.util.AlertUtils;
import client.util.NavigationUtils;
import client.util.ToastUtils;
import javafx.application.Platform;
import javafx.stage.Stage;
import model.Entity.User.Role;
import model.Entity.User.User;

public class AuthHandler {
    public static void loginSuccess(String data, Stage stage) {
        User user = UserParser.parse(data);
        UserSession.setCurrentUser(user);
        if (user == null) return;
        ToastUtils.show(stage, "Welcome " + user.getFullname() + "!");

        if (user.getRole() == Role.ADMIN) {
            NavigationUtils.switchScene(
                    stage,
                    "/fxml/adminManageUser-view.fxml",
                    "Admin Dashboard"
            );
        } else {
            NavigationUtils.switchScene(
                    stage,
                    "/fxml/userHomePage-view.fxml",
                    "Home"
            );

            Platform.runLater(() -> {
                UserHomePageController ctrl =
                        ControllerRegistry.get(
                                UserHomePageController.class
                        );

                if (ctrl != null) {
                    ctrl.setUser(user);
                }
            });
        }
    }

    public static void loginFailed() {
        AlertUtils.error("Wrong username or password.");
        LoginController controller = ControllerRegistry.get(LoginController.class);

        if (controller != null) {
            controller.onLoginFailed();
        }
    }

    public static void registerSuccess(Stage stage) {
        NavigationUtils.switchScene(stage, "/fxml/login-view.fxml", "Login");
        ToastUtils.show(stage, "Account created successfully!");
    }

    public static void registerFailed(String data) {
        AlertUtils.error("Register failed: " + data);
    }

    public static void forgotSuccess(Stage stage) {
        NavigationUtils.switchScene(stage, "/fxml/login-view.fxml", "Login");
        ToastUtils.show(stage, "Password reset successfully!");
    }

    public static void forgotFailed(String data) {
        AlertUtils.error("Reset failed: " + data);
    }
}
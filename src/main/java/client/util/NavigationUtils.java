package client.util;

import client.controller.UserDataReceiver;

import client.network.ClientSocket;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import model.User;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class NavigationUtils {

    private static Stage mainStage;

    private static String currentPage;

    public static void setCurrentPage(String page) {
        currentPage = page;
    }

    public static String getCurrentPage() {
        return currentPage;
    }

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    // ==================== ALERT FACTORY ====================

    private static Alert createAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert;
    }

    // ==================== INFO ====================
    public static void showInfo(
            Stage stage,
            String message
    ) {

        showToast(stage, message);
    }

    // ==================== ERROR ====================

    public static void showError(
            String message
    ) {

        createAlert(
                Alert.AlertType.ERROR,
                "Error",
                message
        ).showAndWait();
    }

    // ==================== WARNING ====================

    public static void showWarning(
            String title,
            String message
    ) {

        createAlert(
                Alert.AlertType.WARNING,
                title,
                message
        ).showAndWait();
    }

    // ==================== CONFIRM ====================

    public static boolean showConfirm(
            String title,
            String message
    ) {

        Optional<ButtonType> result =
                createAlert(
                        Alert.AlertType.CONFIRMATION,
                        title,
                        message
                ).showAndWait();

        return result.isPresent()
                && result.get() == ButtonType.OK;
    }

    // ==================== TOAST ====================

    public static void showToast(
            Stage stage,
            String message
    ) {

        if (stage == null || stage.getScene() == null) {
            return;
        }

        Popup popup = new Popup();

        Label label = new Label(message);

        label.getStyleClass().add("toast-label");

        // fallback style nếu chưa có css
        label.setStyle(
                "-fx-background-color: rgba(0,0,0,0.9);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12 24 12 24;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15,0,0,4);"
        );

        popup.getContent().add(label);

        popup.show(stage);

        label.applyCss();
        label.layout();

        double x =
                stage.getX()
                        + (stage.getWidth() - label.getWidth()) / 2;

        double y =
                stage.getY()
                        + (stage.getHeight() - label.getHeight()) / 2
                        - 80;

        popup.setX(x);
        popup.setY(y);

        PauseTransition delay =
                new PauseTransition(Duration.seconds(2));

        delay.setOnFinished(e ->
                popup.hide()
        );

        delay.play();
    }

    // ==================== LOAD FXML ====================

    public static FXMLLoader loadFXML(
            String fxmlPath
    ) {

        URL url = NavigationUtils.class.getResource(fxmlPath);

        if (url == null) {
            throw new RuntimeException(
                    "FXML NOT FOUND: " + fxmlPath
            );
        }

        return new FXMLLoader(url);
    }

    // ==================== SWITCH SCENE ====================

    public static void switchScene(
            Stage stage,
            String fxmlPath,
            String title
    ) {

        switchScene(
                stage,
                fxmlPath,
                title,
                null,
                null
        );
    }

    // ==================== SWITCH SCENE WITH DATA ====================

    public static void switchScene(
            Stage stage,
            String fxmlPath,
            String title,
            ClientSocket client,
            User user
    ) {

        try {

            FXMLLoader loader =
                    loadFXML(fxmlPath);

            Parent root =
                    loader.load();

            injectUserData(
                    loader.getController(),
                    client,
                    user
            );

            applyStage(
                    stage,
                    root,
                    title
            );

        } catch (IOException e) {
            System.err.println("Navigation Error");
            e.printStackTrace();
        }
    }

    // ==================== APPLY STAGE ====================

    private static void applyStage(
            Stage stage,
            Parent root,
            String title
    ) {

        stage.setScene(new Scene(root));
        stage.setTitle(title);

        stage.centerOnScreen();
        stage.show();
    }

    // ==================== INJECT DATA ====================

    private static void injectUserData(
            Object controller,
            ClientSocket client,
            User user
    ) {

        if (controller instanceof UserDataReceiver receiver) {

            receiver.setClient(client);
            receiver.setUser(user);
        }
    }
}


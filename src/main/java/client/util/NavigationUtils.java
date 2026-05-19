package client.util;

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

import java.io.IOException;
import java.util.Optional;

public class NavigationUtils {

    // ==================== TOAST ====================

    public static void showInfo(
            Stage stage,
            String message
    ) {

        showToast(stage, message);
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showToast(
            Stage stage,
            String message
    ) {

        if (stage == null || stage.getScene() == null) {
            return;
        }

        Popup popup = new Popup();

        Label label = new Label(message);

        label.setStyle(
                "-fx-background-color: rgba(0,0,0,0.9);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12 24 12 24;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        popup.getContent().add(label);

        popup.show(stage.getScene().getWindow());

        label.applyCss();
        label.layout();

        double x =
                stage.getX()
                        + (stage.getWidth() - label.getWidth()) / 2;

        double y =
                stage.getY()
                        + stage.getHeight()
                        - 100;

        popup.setX(x);
        popup.setY(y);

        PauseTransition delay =
                new PauseTransition(
                        Duration.seconds(2)
                );

        delay.setOnFinished(e ->
                popup.hide()
        );

        delay.play();
    }

    // ==================== CONFIRM ALERT ====================

    public static boolean showConfirm(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        Optional<ButtonType> result =
                alert.showAndWait();

        return result.isPresent()
                && result.get() == ButtonType.OK;
    }

    // ==================== WARNING ALERT ====================

    public static void showWarning(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    // ==================== SWITCH SCENE ====================

    public static void switchScene(
            Stage stage,
            String fxmlPath,
            String title
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            NavigationUtils.class
                                    .getResource(fxmlPath)
                    );

            Parent root = loader.load();

            stage.setScene(new Scene(root));

            stage.setTitle(title);

            stage.centerOnScreen();

        } catch (IOException e) {

            System.err.println(
                    "Error: " + e.getMessage()
            );

            showWarning(
                    "Navigation Error",
                    "Could not load screen: " + title
            );
        }
    }
}
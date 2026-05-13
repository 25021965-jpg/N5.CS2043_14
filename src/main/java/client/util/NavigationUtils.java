package client.util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NavigationUtils {

    // --- 1. HÀM HIỂN THỊ THÔNG BÁO ---

    public static void showInfo(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.show();

        PauseTransition delay =
                new PauseTransition(
                        Duration.seconds(0.5)
                );

        delay.setOnFinished(e ->
                alert.close()
        );
        delay.play();
    }

    public static void showError(String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // --- 2. HÀM CHUYỂN TRANG ---

    public static void switchScene(Stage stage, String fxmlPath, String title) {
            try {
                FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource(fxmlPath));
                Parent root = loader.load();
                stage.setScene(new Scene(root));
                stage.setTitle(title);
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
                showError("Could not load screen: " + title);
            }
    }

    public static void showToast(
            Stage stage,
            String message
    ) {

        Popup popup = new Popup();

        Label label = new Label(message);

        label.setStyle(
                "-fx-background-color: black;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12 24 12 24;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14px;"
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
                        + (stage.getHeight() - label.getHeight()) / 2;

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
}

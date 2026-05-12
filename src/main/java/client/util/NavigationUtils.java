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

public class NavigationUtils {

    // --- 1. HÀM HIỂN THỊ THÔNG BÁO ---

    public static void showInfo(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
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
}

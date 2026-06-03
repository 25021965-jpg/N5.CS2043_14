package client.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class AlertUtils {

    private AlertUtils() {}

    private static Alert create(
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

    public static void error(String message) {
        create(
                Alert.AlertType.ERROR,
                "Error",
                message
        ).showAndWait();
    }

    public static void warning(
            String title,
            String message
    ) {
        create(
                Alert.AlertType.WARNING,
                title,
                message
        ).showAndWait();
    }

    public static boolean confirm(
            String title,
            String message
    ) {
        Optional<ButtonType> result =
                create(
                        Alert.AlertType.CONFIRMATION,
                        title,
                        message
                ).showAndWait();

        return result.isPresent()
                && result.get() == ButtonType.OK;
    }
}
package client.util;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class ToastUtils {

    private ToastUtils() {}

    public static void show(
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
        popup.show(stage);

        label.applyCss();
        label.layout();

        popup.setX(
                stage.getX()
                        + (stage.getWidth() - label.getWidth()) / 2
        );

        popup.setY(
                stage.getY()
                        + (stage.getHeight() - label.getHeight()) / 2
                        - 80
        );

        PauseTransition delay =
                new PauseTransition(Duration.seconds(2));

        delay.setOnFinished(e -> popup.hide());
        delay.play();
    }
}
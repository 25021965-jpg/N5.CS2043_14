package client.network;

import client.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    private static ClientSocket socket;
    @Override
    public void start(Stage stage) throws Exception {
        socket = new ClientSocket();

        FXMLLoader fxmlLoader =
                new FXMLLoader(ClientApp.class.getResource("/fxml/login-view.fxml"));

        LoginController ctrl = fxmlLoader.getController();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("Auction System");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
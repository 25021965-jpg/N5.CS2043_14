package client.network;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import client.controller.LoginController;

public class ClientApp extends Application {
    private static ClientSocket socket;

    @Override
    public void start(Stage stage) throws Exception {

        ResponseHandler.setMainStage(stage);

        // Lấy Instance duy nhất
        socket = ClientSocket.getInstance();

        if (socket != null) {
            socket.listen(); // Bắt đầu nghe server ngay từ khi bật App
        }

        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("/fxml/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        LoginController loginController= fxmlLoader.getController();
        loginController.setStage(stage);
        loginController.setClient(socket);

        stage.setScene(scene);
        stage.setTitle("Auction System");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
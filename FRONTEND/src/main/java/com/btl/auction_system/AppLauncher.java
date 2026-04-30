package com.btl.auction_system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppLauncher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Chú ý đường dẫn: tính từ thư mục resources trở đi
        FXMLLoader fxmlLoader = new FXMLLoader(AppLauncher.class.getResource("view/login-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Ứng dụng Đấu Giá");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
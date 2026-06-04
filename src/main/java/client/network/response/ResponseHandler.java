package client.network.response;

import javafx.stage.Stage;

import java.util.function.Consumer;

public class ResponseHandler {

    private static Stage mainStage;
    private static Consumer<String> liveAuctionListener;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }
    public static void setLiveAuctionListener(Consumer<String> listener) {
        liveAuctionListener = listener;
    }

    public static void handle(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) return;
        ResponseRouter.route(rawMessage, mainStage);
    }
}
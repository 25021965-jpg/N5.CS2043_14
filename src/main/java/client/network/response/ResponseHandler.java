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

        // ===== FAST PATH LIVE AUCTION =====
        if (isLiveAuctionMessage(rawMessage)) {
            if (liveAuctionListener != null) {
                liveAuctionListener.accept(rawMessage);
            }
            return;
        }

        ResponseRouter.route(rawMessage, mainStage);
    }

    private static boolean isLiveAuctionMessage(String msg) {
        if (msg.startsWith("UPDATE_PRICE")) {
            System.out.println("🔥 [ResponseHandler] UPDATE_PRICE detected, listener=" + (liveAuctionListener != null ? "OK" : "NULL"));
        }
        return msg.startsWith("UPDATE_PRICE")
                || msg.startsWith("JOIN_SUCCESS")
                || msg.startsWith("JOIN_FAILED")
                || msg.startsWith("BID_FAILED")
                || msg.startsWith("BID_HISTORY_SUCCESS")
                || msg.startsWith("BID_HISTORY_EMPTY")
                || msg.startsWith("AUCTION_ENDED")
                || msg.startsWith("YOU_WON")
                || msg.startsWith("VIRTUAL_BALANCE")
                || msg.startsWith("WINNER_BALANCE")
                || msg.startsWith("SELLER_BALANCE")
                || msg.startsWith("ERROR");
    }
}
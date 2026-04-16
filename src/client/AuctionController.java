package client;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.application.Platform;

public class AuctionController {

    @FXML
    private TextField bidInput;

    @FXML
    private TextArea logArea;

    private SocketClient client;

    public void initialize() {
        try {
            client = new SocketClient();

            // nhận realtime từ server
            client.listen(msg -> {
                Platform.runLater(() -> {
                    logArea.appendText(msg + "\n");
                });
            });

        } catch (Exception e) {
            logArea.appendText("Cannot connect server\n");
        }
    }

    @FXML
    public void handleBid() {
        String bid = bidInput.getText();

        if (bid.isEmpty()) return;

        client.sendBid(bid);
        bidInput.clear();
    }
}
package client;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class AuctionController {

    @FXML
    private TextField bidInput;

    @FXML
    private TextArea logArea;

    private SocketClient client;

    @FXML
    private Label currentPriceLabel;

    public void initialize() {
        try {
            client = new SocketClient();

            // nhận realtime từ server
            client.listen(msg -> {
                Platform.runLater(() -> {

                    logArea.appendText(msg + "\n");

                    if (msg.startsWith("NEW BID:")) {
                        try {

                            String price = msg.substring("NEW BID:".length()).trim();

                            currentPriceLabel.setText("Current Price: " + price);

                            currentPriceLabel.setStyle("-fx-text-fill: green; -fx-font-size: 18px;");

                            PauseTransition pt = new PauseTransition(Duration.seconds(0.5));
                            pt.setOnFinished(e ->
                                    currentPriceLabel.setStyle("-fx-font-size: 18px;")
                            );
                            pt.play();

                        } catch (Exception e) {
                            System.out.println("Parse error: " + msg);
                        }
                    }
                });
            });

    client.sendLogin("user@gmail.com", "456");


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
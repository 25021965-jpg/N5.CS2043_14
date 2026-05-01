package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AuctionController {

    // ===== UI =====
    @FXML private VBox loginBox;
    @FXML private VBox auctionBox;

    @FXML private TextField logEmail;
    @FXML private PasswordField logPassword;

    @FXML private TextField regUsername;
    @FXML private TextField regEmail;
    @FXML private PasswordField regPassword;

    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private Label currentPriceLabel;

    @FXML private TextField bidInput;
    @FXML private TextField auctionIdInput;
    @FXML private TextArea logArea;

    // ===== DATA =====
    private SocketClient client;

    // ===== INIT =====
    @FXML
    public void initialize() {
        showAuctionUI(false);

        try {
            client = new SocketClient();
            client.listen(msg -> Platform.runLater(() -> handleServerResponse(msg)));
            statusLabel.setText("System Ready");
        } catch (Exception e) {
            statusLabel.setText("Cannot connect to server!");
            client = null;
        }
    }

    // ===== SERVER HANDLE =====
    private void handleServerResponse(String msg) {
        log("[Server]: " + msg);

        if (msg.startsWith("LOGIN_SUCCESS") || msg.startsWith("REGISTER_SUCCESS")) {
            handleLoginSuccess(msg);
        }
        else if (msg.startsWith("NEW_BID:")) {
            handleNewBid(msg);
        }
        else if (msg.startsWith("ERROR")) {
            statusLabel.setText(msg);
        }
        else if (msg.startsWith("BID_SUCCESS")) {
            statusLabel.setText("Bid placed successfully!");
        }
        else if (msg.startsWith("BID_FAILED")) {
            statusLabel.setText("Bid failed!");
        }
    }

    private void handleLoginSuccess(String msg) {
        String[] parts = msg.split(" ");
        String name = parts.length > 1 ? parts[1] : "User";

        userLabel.setText("Welcome, " + name);
        statusLabel.setText("Login successful!");
        showAuctionUI(true);
    }

    private void handleNewBid(String msg) {
        try {
            // Debug xem server gửi gì
            log("[DEBUG BID] " + msg);

            // Tách giá từ message
            String[] parts = msg.split(":");
            if (parts.length < 2) {
                log("Invalid NEW_BID format!");
                return;
            }

            double price = Double.parseDouble(parts[1].trim());

            // Update UI
            currentPriceLabel.setText(String.format("$%.2f", price));

        } catch (Exception e) {
            log("Error parsing bid: " + msg);
        }
    }

    // ===== ACTIONS =====
    @FXML
    public void handleLogin() {
        if (!isClientReady()) return;

        String email = logEmail.getText().trim();
        String pass = logPassword.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill login info");
            return;
        }

        client.sendLogin(email, pass);
    }

    @FXML
    public void handleRegister() {
        if (!isClientReady()) return;

        String user = regUsername.getText().trim();
        String email = regEmail.getText().trim();
        String pass = regPassword.getText().trim();

        if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill register info");
            return;
        }

        client.sendRegister(user, email, pass, "BIDDER");
    }

    @FXML
    public void handleLogout() {
        if (!isClientReady()) return;

        client.sendLogout();
        resetUI();
        showAuctionUI(false);
        statusLabel.setText("Logged out.");
    }

    @FXML
    public void handleBid() {
        if (!isClientReady()) return;

        String id = auctionIdInput.getText().trim();
        String amount = bidInput.getText().trim();

        if (id.isEmpty() || amount.isEmpty()) {
            log("Error: Fill ID and Bid amount");
            return;
        }

        if (!isValidBid(amount)) {
            log("Invalid bid amount!");
            return;
        }

        client.sendBid(id, amount);
        bidInput.clear();
    }

    // ===== HELPER =====
    private boolean isClientReady() {
        if (client == null) {
            statusLabel.setText("Not connected to server!");
            return false;
        }
        return true;
    }

    private boolean isValidBid(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showAuctionUI(boolean loggedIn) {
        loginBox.setVisible(!loggedIn);
        loginBox.setManaged(!loggedIn);

        auctionBox.setVisible(loggedIn);
        auctionBox.setManaged(loggedIn);
    }

    private void resetUI() {
        logEmail.clear();
        logPassword.clear();
        regUsername.clear();
        regEmail.clear();
        regPassword.clear();

        auctionIdInput.clear();
        bidInput.clear();

        currentPriceLabel.setText("$0");
        userLabel.setText("");
        logArea.clear();
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }
}
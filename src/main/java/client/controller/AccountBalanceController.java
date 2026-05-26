package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class AccountBalanceController implements UserDataReceiver {

    private ClientSocket client;
    private User currentUser;

    @FXML private Button infoBtn, historyBtn, createdAuctionBtn, favoriteBtn, balanceBtn;
    @FXML private Button logoutBtn, deleteAccountBtn, backHomeBtn;
    @FXML private Label balanceLabel;
    @FXML private TextField depositField, withdrawField;
    @FXML private VBox transactionContainer;
    @FXML private ComboBox<String> filterBox;

    private static AccountBalanceController instance;

    public AccountBalanceController() {
        instance = this;
    }

    public static AccountBalanceController getInstance() {
        return instance;
    }

    public void setClient(ClientSocket client) {
        this.client = client;
        if (client != null) {
            client.setMessageListener(this::handleServerMessage);
        }
    }

    @Override
    public void setUser(User user) {
        if (user == null) {
            System.out.println("Received NULL user");
            return;
        }
        this.currentUser = user;
        if (currentUser.getBalance() == null) {
            currentUser.setBalance(BigDecimal.ZERO);
        }
        updateBalance();
        if (client != null && currentUser != null) {
            client.sendGetTransactions(currentUser.getUser_id());
        }
    }

    @FXML
    public void initialize() {
        System.out.println("Account Balance Loaded");
        setupMenuEvents();
        if (currentUser != null) {
            updateBalance();
        }
        filterBox.getItems().addAll(
                "All",
                "Deposit",
                "Withdraw",
                "Paid",
                "Received"
        );

        filterBox.setValue("All");

        filterBox.setOnAction(e -> {
            if (client != null && currentUser != null) {
                client.sendGetTransactions(currentUser.getUser_id());
            }
        });
    }

    private void handleServerMessage(String msg) {
        javafx.application.Platform.runLater(() -> {
            System.out.println("[BalanceController] Received: " + msg);

            if (msg.startsWith("BALANCE_UPDATE_SUCCESS")) {
                String[] parts = msg.split("\\|");
                if (parts.length >= 2) {
                    BigDecimal newBalance = new BigDecimal(parts[1]);
                    currentUser.setBalance(newBalance);
                    updateBalance();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Transaction successful!");
                    if (client != null && currentUser != null) {
                        client.sendGetTransactions(currentUser.getUser_id());
                    }
                }
            } else if (msg.startsWith("BALANCE_UPDATE_FAILED")) {
                String errorMsg = msg.split("\\|").length > 1 ? msg.split("\\|")[1] : "Transaction failed";
                showAlert(Alert.AlertType.ERROR, "Error", errorMsg);
            } else if (msg.startsWith("TRANSACTIONS_LIST")) {
                String transactionsData = msg.substring("TRANSACTIONS_LIST|".length());
                updateTransactionList(transactionsData);
            }
        });
    }

    @FXML
    public void handleDeposit() {
        if (client == null) {
            client = ClientSocket.getInstance();
            if (client == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Not connected to server!");
                return;
            }
        }
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user logged in.");
            return;
        }

        String input = depositField.getText().trim();
        if (input.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Deposit Error", "Please enter an amount.");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(input);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Deposit Error", "Amount must be greater than 0.");
                return;
            }

            client.sendDeposit(currentUser.getUser_id(), amount);
            depositField.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Deposit Error", "Invalid amount.");
        }
    }

    //  ĐỔI TỪ private THÀNH @FXML public
    @FXML
    public void handleWithdraw() {
        if (client == null) {
            client = ClientSocket.getInstance();
            if (client == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Not connected to server!");
                return;
            }
        }
            if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user logged in.");
            return;
        }

        String input = withdrawField.getText().trim();
        if (input.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Withdraw Error", "Please enter an amount.");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(input);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Withdraw Error", "Amount must be greater than 0.");
                return;
            }

            client.sendWithdraw(currentUser.getUser_id(), amount);
            withdrawField.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Withdraw Error", "Invalid amount.");
        }
    }

    private void updateBalance() {
        if (currentUser == null) return;
        if (currentUser.getBalance() == null) {
            currentUser.setBalance(BigDecimal.ZERO);
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        balanceLabel.setText(format.format(currentUser.getBalance()) + " USD");
    }

    public void updateTransactionList(String data) {
        transactionContainer.getChildren().clear();

        if (data == null || data.isEmpty()) {
            Label emptyLabel = new Label("No transactions yet");
            emptyLabel.setStyle("-fx-text-fill:#64748B; -fx-padding:20;");
            transactionContainer.getChildren().add(emptyLabel);
            return;
        }

        String[] transactions = data.split("\\|");

        for (String tx : transactions) {
            String[] parts = tx.split(";");

            if (parts.length >= 4) {

                String type = parts[0];
                String selected = filterBox.getValue();
                if (selected != null && !"All".equals(selected)) {
                    if ("Deposit".equals(selected) && !type.equals("DEPOSIT")) continue;
                    if ("Withdraw".equals(selected) && !type.equals("WITHDRAW")) continue;
                    if ("Paid".equals(selected) && !type.equals("TRANSFER_OUT")) continue;
                    if ("Received".equals(selected) && !type.equals("TRANSFER_IN")) continue;
                }

                String amount = parts[1];
                String time = parts[2];
                String desc = parts[3];

                HBox card = new HBox(20);
                card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                card.setStyle("""
                -fx-background-color:white;
                -fx-background-radius:12;
                -fx-border-radius:12;
                -fx-border-color:#E2E8F0;
                -fx-padding:15;
            """);

                VBox left = new VBox(5);

                Label title = getLabel(type, desc);

                Label timeLabel = new Label(time);
                timeLabel.setStyle("""
                -fx-text-fill:#64748B;
                -fx-font-size:13px;
            """);

                left.getChildren().addAll(title, timeLabel);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label amountLabel = getAmountLabel(type, amount);

                card.getChildren().addAll(left, spacer, amountLabel);
                transactionContainer.getChildren().add(card);
            }
        }
    }

    private static Label getAmountLabel(String type, String amount) {
        boolean positive =
                type.equals("DEPOSIT")
                        || type.equals("TRANSFER_IN");

        Label amountLabel = new Label(
                (positive ? "+ " : "- ") + amount + " USD"
        );

        amountLabel.setStyle(
                positive
                        ? "-fx-text-fill:#16A34A; -fx-font-size:18px; -fx-font-weight:bold;"
                        : "-fx-text-fill:#EF4444; -fx-font-size:18px; -fx-font-weight:bold;"
        );
        return amountLabel;
    }

    private static Label getLabel(String type, String desc) {
        String titleText = switch (type) {
            case "DEPOSIT" -> "Deposit Successfully";
            case "WITHDRAW" -> "Withdraw Successfully";
            case "TRANSFER_OUT" -> "Paid to " + desc;
            case "TRANSFER_IN" -> "Received from " + desc;
            default -> type;
        };

        Label title = new Label(titleText);
        title.setStyle("""
        -fx-font-size:16px;
        -fx-font-weight:bold;
        -fx-text-fill:#0F172A;
    """);
        return title;
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");

        ButtonType logoutButton = new ButtonType("Logout");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(logoutButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == logoutButton) {
            if (client != null) {
                client.sendLogout();
            }
            UserSession.setCurrentUser(null);
            openPage("/fxml/login-view.fxml", "Login");
        }
    }

    private void setupMenuEvents() {
        infoBtn.setOnAction(e -> openPage("/fxml/userProfile-view.fxml", "Profile"));
        historyBtn.setOnAction(e -> openPage("/fxml/auctionHistory-view.fxml", "Auction History"));
        createdAuctionBtn.setOnAction(e -> openPage("/fxml/myAuctions-view.fxml", "My Auctions"));
        favoriteBtn.setOnAction(e -> openPage("/fxml/Favourite-view.fxml", "Favourite"));
        balanceBtn.setOnAction(e -> openPage("/fxml/accountBalance-view.fxml", "Account Balance"));
        backHomeBtn.setOnAction(e -> openPage("/fxml/HomePage.fxml", "Home"));
        logoutBtn.setOnAction(e -> handleLogout());
        deleteAccountBtn.setOnAction(e -> handleDeleteAccount());
    }

    private void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Delete Account");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete your account?");
        ButtonType deleteButton = new ButtonType("Delete");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
            openPage("/fxml/login-view.fxml", "Login");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (loader.getController() instanceof UserDataReceiver c) {
                c.setClient(client);
                c.setUser(currentUser);
            }
            Stage stage = (Stage) backHomeBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void updateBalanceFromServer(BigDecimal newBalance) {
        if (currentUser != null) {
            currentUser.setBalance(newBalance);
            updateBalance();
        }
    }
}
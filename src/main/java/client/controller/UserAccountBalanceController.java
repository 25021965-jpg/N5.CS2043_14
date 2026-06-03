package client.controller;

import client.manager.ControllerRegistry;
import client.manager.UserSession;
import client.network.ClientSocket;
import client.util.AlertUtils;
import client.util.TextUtils;

import client.util.ToastUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import model.*;
import model.Entity.Item.*;
import model.Entity.User.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAccountBalanceController extends BaseController implements UserDataReceiver {
    private static final Logger LOGGER =
            Logger.getLogger(UserAccountBalanceController.class.getName());

    private static final List<String> FILTERS = List.of(
            "All",
            "Deposit",
            "Withdraw",
            "Paid",
            "Received"
    );

    @FXML private Label balanceLabel;
    @FXML private TextField depositField;
    @FXML private TextField withdrawField;
    @FXML private VBox transactionContainer;
    @FXML private ComboBox<String> filterBox;

    protected User currentUser;
    // ==================== INIT ====================

    @FXML
    public void initialize() {
        ControllerRegistry.register(UserAccountBalanceController.class, this);
        System.out.println("Account Balance Loaded");
        setupFilterBox();


        if (client == null) {
            client = ClientSocket.getInstance();
        }
        if (client != null) {
            client.addListener("AccountBalance", this::handleServerMessage);
        }
    }

    private void setupFilterBox() {
        filterBox.getItems().addAll(FILTERS);
        filterBox.setValue("All");
        filterBox.setOnAction(e ->
                loadTransactions()
        );
    }

    // ==================== USER ====================

    @Override
    public void setUser(User user) {
        if (user == null) {
            AlertUtils.error("No user data found.");
            return;
        }

        this.currentUser = user;
        updateBalance();
        loadTransactions();
    }

    // ==================== CLIENT ====================

    @Override
    public void setClient(
            ClientSocket client
    ) {

        if (client == null) {
            return;
        }
        this.client = client;
        client.addListener("AccountBalance", this::handleServerMessage);
    }



    // ==================== SERVER ====================

    private void handleServerMessage(String msg) {
        if (msg == null) return;

        if (msg.startsWith("TRANSACTIONS_LIST")) {
            System.out.println("TRANSACTIONS_LIST detected");            String data = msg.substring("TRANSACTIONS_LIST|".length());
            System.out.println("Data: " + data);
            updateTransactionList(data);
            return;
        }

        if (msg.startsWith("BALANCE_UPDATE_SUCCESS")) {
            Platform.runLater(() -> handleBalanceSuccess(msg));
        } else if (msg.startsWith("BALANCE_UPDATE_FAILED")) {
            Platform.runLater(() -> handleBalanceFailed(msg));
        }
    }

    private void handleBalanceSuccess(String msg) {
        String[] parts = msg.split("\\|");
        if (parts.length < 2) return;

        BigDecimal newBalance = new BigDecimal(parts[1]);
        currentUser.setBalance(newBalance);
        UserSession.setCurrentUser(currentUser);

        updateBalance();

        ToastUtils.show(
                (javafx.stage.Stage) balanceLabel.getScene().getWindow(),
                "Transaction successful!"
        );

        client.addListener("AccountBalance", this::handleServerMessage);
        loadTransactions();
    }

    private void handleBalanceFailed(String msg) {
        String[] parts = msg.split("\\|");
        String errorMessage =
                parts.length > 1
                        ? parts[1]
                        : "Transaction failed.";
        AlertUtils.error(errorMessage);
    }

    private void handleTransactionList(String msg) {
        System.out.println("handleTransactionList received: " + msg);
        String data = msg.substring("TRANSACTIONS_LIST|".length());
        System.out.println("data after substring: '" + data + "'");
        updateTransactionList(data);
    }

    // ==================== DEPOSIT ====================

    @FXML
    public void handleDeposit() {

        BigDecimal amount =
                parseAmount(
                        depositField.getText(),
                        "Deposit"
                );

        if (amount == null) {
            return;
        }

        client.sendDeposit(
                currentUser.getUser_id(),
                amount
        );

        depositField.clear();
    }

    // ==================== WITHDRAW ====================

    @FXML
    public void handleWithdraw() {

        BigDecimal amount =
                parseAmount(
                        withdrawField.getText(),
                        "Withdraw"
                );

        if (amount == null) {
            return;
        }

        client.sendWithdraw(
                currentUser.getUser_id(),
                amount
        );

        withdrawField.clear();
    }

    // ==================== AMOUNT VALIDATION ====================

    private BigDecimal parseAmount(
            String input,
            String action
    ) {

        if (input == null || input.isBlank()) {

            AlertUtils.error(
                    action + " amount is required."
            );

            return null;
        }

        try {

            BigDecimal amount =
                    new BigDecimal(input.trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                AlertUtils.error(action + " amount must be greater than 0.");

                return null;
            }
            return amount;

        } catch (NumberFormatException e) {
            AlertUtils.error("Invalid amount.");
            return null;
        }
    }

    // ==================== BALANCE ====================
    private void updateBalance() {
        Platform.runLater(() -> {
            if (currentUser == null) return;
            if (currentUser.getBalance() == null) {
                currentUser.setBalance(BigDecimal.ZERO);
            }
            String formatted = TextUtils.formatCurrency(currentUser.getBalance());
            System.out.println(" Formatted balance: " + formatted);
            balanceLabel.setText(formatted != null ? formatted : "0 USD");
            System.out.println(" Balance updated: " + currentUser.getBalance());
        });
        if (balanceLabel == null) {
            System.out.println("balanceLabel is NULL! Check FXML fx:id");
        }
    }

    public void updateBalanceFromServer(BigDecimal newBalance) {
        if (currentUser == null) {
            return;
        }
        currentUser.setBalance(newBalance);
        updateBalance();
    }

    // ==================== TRANSACTIONS ====================

    private void loadTransactions() {
        if (currentUser == null) return;
        if (client == null) client = ClientSocket.getInstance();

        // Luôn đảm bảo listener đúng
        client.addListener("AccountBalance", this::handleServerMessage);

        System.out.println("loadTransactions: sending request for user " + currentUser.getUser_id());
        client.sendGetTransactions(currentUser.getUser_id());
    }

    public void updateTransactionList(String data) {
        Platform.runLater(() -> {
            try {
                if (transactionContainer == null) {
                    System.out.println("transactionContainer is NULL!");
                    return;
                }

                transactionContainer.getChildren().clear();

                if (data == null || data.isBlank()) {
                    transactionContainer.getChildren().add(new Label("No transactions yet"));
                    return;
                }

                // Split theo | nhưng bỏ phần tử rỗng cuối (server gửi trailing |)
                String[] transactions = data.split("\\|");
                boolean hasAny = false;

                for (String tx : transactions) {
                    if (tx.isBlank()) continue;

                    String[] parts = tx.split(";");
                    if (parts.length >= 4) {
                        String type   = parts[0]; // DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT
                        String amount = parts[1];
                        String time   = parts[2];
                        String desc   = parts[3];

                        if (!shouldDisplay(type)) continue;

                        HBox card = createTransactionCard(type, amount, time, desc);
                        transactionContainer.getChildren().add(card);
                        hasAny = true;
                    }
                }

                if (!hasAny) {
                    transactionContainer.getChildren().add(new Label("No transactions yet"));
                }

            } catch (Exception e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Failed to update transaction list",
                        e
                );
            }
        });
    }


    // ==================== FILTER ====================

    private boolean shouldDisplay(String type) {
        String selected = filterBox.getValue();
        System.out.println(" shouldDisplay: selected=" + selected + ", type=" + type);

        if (selected == null || selected.equals("All")) {
            return true;
        }

        boolean result = switch (selected) {
            case "Deposit" -> type.equals("DEPOSIT");
            case "Withdraw" -> type.equals("WITHDRAW");
            case "Paid" -> type.equals("TRANSFER_OUT");
            case "Received" -> type.equals("TRANSFER_IN");
            default -> true;
        };
        System.out.println(" shouldDisplay result: " + result);
        return result;
    }

    // ==================== CARD ====================

    private HBox createTransactionCard(String type, String amount, String time, String desc) {
        try {
            HBox card = new HBox(20);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-radius:12; -fx-border-color:#E2E8F0; -fx-padding:15;");

            VBox left = new VBox(5);
            Label title = createTitleLabel(type, desc);
            Label timeLabel = createTimeLabel(time);
            left.getChildren().addAll(title, timeLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label amountLabel = createAmountLabel(type, amount);

            card.getChildren().addAll(left, spacer, amountLabel);
            return card;
        } catch (Exception e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Failed to create transaction card",
                    e
            );
            return new HBox(new Label("Error loading transaction"));
        }
    }

    // ==================== LABELS ====================

    private Label createTitleLabel(
            String type,
            String desc
    ) {

        String titleText =
                switch (type) {

                    case "DEPOSIT" ->
                            "Deposit Successfully";

                    case "WITHDRAW" ->
                            "Withdraw Successfully";

                    case "TRANSFER_OUT" ->
                            "Paid to " + desc;

                    case "TRANSFER_IN" ->
                            "Received from " + desc;

                    default -> type;
                };

        Label label =
                new Label(titleText);

        label.setStyle("""
                -fx-font-size:16px;
                -fx-font-weight:bold;
                -fx-text-fill:#0F172A;
                """);

        return label;
    }

    private Label createTimeLabel(
            String time
    ) {

        Label label =
                new Label(time);

        label.setStyle("""
                -fx-text-fill:#64748B;
                -fx-font-size:13px;
                """);

        return label;
    }

    private Label createAmountLabel(
            String type,
            String amount
    ) {

        boolean positive =
                type.equals("DEPOSIT")
                        || type.equals("TRANSFER_IN");

        Label label =
                new Label(
                        (positive ? "+ " : "- ")
                                + amount
                                + " USD"
                );

        label.setStyle(
                positive
                        ? "-fx-text-fill:#16A34A; -fx-font-size:18px; -fx-font-weight:bold;"
                        : "-fx-text-fill:#EF4444; -fx-font-size:18px; -fx-font-weight:bold;"
        );

        return label;
    }
}


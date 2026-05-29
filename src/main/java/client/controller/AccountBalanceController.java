package client.controller;

import client.network.ClientSocket;
import client.util.TextUtils;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import model.User;

import java.math.BigDecimal;
import java.util.List;

import static client.util.NavigationUtils.showToast;

public class AccountBalanceController
        extends BaseController
        implements UserDataReceiver {
    private static AccountBalanceController instance;

    public static AccountBalanceController getInstance() {
        return instance;
    }
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
        instance = this;
        System.out.println("Account Balance Loaded");

        setupFilterBox();
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
            showError("No user data found.");
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
        client.setMessageListener(
                this::handleServerMessage
        );
    }



    // ==================== SERVER ====================

    private void handleServerMessage(
            String msg
    ) {

        runUI(() -> {

            System.out.println(
                    "[BalanceController] Received: "
                            + msg
            );

            if (msg.startsWith("BALANCE_UPDATE_SUCCESS")) {

                handleBalanceSuccess(msg);

            } else if (msg.startsWith("BALANCE_UPDATE_FAILED")) {

                handleBalanceFailed(msg);

            } else if (msg.startsWith("TRANSACTIONS_LIST")) {

                handleTransactionList(msg);
            }
        });
    }

    private void handleBalanceSuccess(
            String msg
    ) {

        String[] parts =
                msg.split("\\|");

        if (parts.length < 2) {
            return;
        }

        BigDecimal newBalance =
                new BigDecimal(parts[1]);

        currentUser.setBalance(newBalance);

        updateBalance();

        showToast(
                (javafx.stage.Stage)
                        balanceLabel.getScene().getWindow(),
                "Transaction successful!"
        );

        loadTransactions();
    }

    private void handleBalanceFailed(
            String msg
    ) {

        String[] parts =
                msg.split("\\|");

        String errorMessage =
                parts.length > 1
                        ? parts[1]
                        : "Transaction failed.";

        showError(errorMessage);
    }

    private void handleTransactionList(
            String msg
    ) {

        String data =
                msg.substring(
                        "TRANSACTIONS_LIST|".length()
                );

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

            showError(
                    action + " amount is required."
            );

            return null;
        }

        try {

            BigDecimal amount =
                    new BigDecimal(input.trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                showError(
                        action
                                + " amount must be greater than 0."
                );

                return null;
            }

            return amount;

        } catch (NumberFormatException e) {

            showError("Invalid amount.");

            return null;
        }
    }

    // ==================== BALANCE ====================

    private void updateBalance() {

        if (currentUser == null) {
            return;
        }

        if (currentUser.getBalance() == null) {
            currentUser.setBalance(BigDecimal.ZERO);
        }

        balanceLabel.setText(
                TextUtils.formatCurrency(
                        currentUser.getBalance()
                )
        );
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
        if (currentUser == null) {
            return;
        }

        client.sendGetTransactions(
                currentUser.getUser_id()
        );
    }

    public void updateTransactionList(
            String data
    ) {

        transactionContainer
                .getChildren()
                .clear();

        if (data == null || data.isBlank()) {

            Label emptyLabel =
                    new Label("No transactions yet");

            emptyLabel.setStyle(
                    "-fx-text-fill:#64748B; -fx-padding:20;"
            );

            transactionContainer
                    .getChildren()
                    .add(emptyLabel);

            return;
        }

        String[] transactions =
                data.split("\\|");

        for (String tx : transactions) {

            String[] parts =
                    tx.split(";");

            if (parts.length < 4) {
                continue;
            }

            String type = parts[0];

            if (!shouldDisplay(type)) {
                continue;
            }

            String amount = parts[1];
            String time = parts[2];
            String desc = parts[3];

            transactionContainer
                    .getChildren()
                    .add(
                            createTransactionCard(
                                    type,
                                    amount,
                                    time,
                                    desc
                            )
                    );
        }
    }

    // ==================== FILTER ====================

    private boolean shouldDisplay(
            String type
    ) {

        String selected =
                filterBox.getValue();

        if (selected == null
                || selected.equals("All")) {

            return true;
        }

        return switch (selected) {

            case "Deposit" ->
                    type.equals("DEPOSIT");

            case "Withdraw" ->
                    type.equals("WITHDRAW");

            case "Paid" ->
                    type.equals("TRANSFER_OUT");

            case "Received" ->
                    type.equals("TRANSFER_IN");

            default -> true;
        };
    }

    // ==================== CARD ====================

    private HBox createTransactionCard(
            String type,
            String amount,
            String time,
            String desc
    ) {

        HBox card =
                new HBox(20);

        card.setAlignment(Pos.CENTER_LEFT);

        card.setStyle("""
                -fx-background-color:white;
                -fx-background-radius:12;
                -fx-border-radius:12;
                -fx-border-color:#E2E8F0;
                -fx-padding:15;
                """);

        VBox left =
                new VBox(5);

        Label title =
                createTitleLabel(type, desc);

        Label timeLabel =
                createTimeLabel(time);

        left.getChildren().addAll(
                title,
                timeLabel
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label amountLabel =
                createAmountLabel(
                        type,
                        amount
                );

        card.getChildren().addAll(
                left,
                spacer,
                amountLabel
        );

        return card;
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


package client.network.response.handler;

import client.controller.AccountBalanceController;
import client.controller.LiveAuctionController;
import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class BalanceHandler {

    public static void success(String raw, Stage stage) {

        String[] parts = raw.split("\\|");
        if (parts.length < 2) return;

        BigDecimal newBalance = new BigDecimal(parts[1]);

        if (AccountBalanceController.getInstance() != null) {
            AccountBalanceController.getInstance()
                    .updateBalanceFromServer(newBalance);
        }

        if (LiveAuctionController.getInstance() != null) {
            LiveAuctionController.getInstance()
                    .updateBalance(newBalance);
        }

        NavigationUtils.showToast(stage, "Balance updated!");
    }

    public static void failed(String data, Stage stage) {
        NavigationUtils.showError("Balance update failed: " + data);
    }

    public static void transactions(String data) {

        AccountBalanceController ctrl = AccountBalanceController.getInstance();
        if (ctrl != null) ctrl.updateTransactionList(data);
    }
}
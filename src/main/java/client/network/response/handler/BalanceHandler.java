package client.network.response.handler;

import client.controller.UserAccountBalanceController;
import client.controller.UserLiveAuctionController;
import client.manager.ControllerRegistry;
import client.util.AlertUtils;

import client.util.ToastUtils;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class BalanceHandler {

    private static final String SUCCESS_PREFIX =
            "BALANCE_UPDATE_SUCCESS|";

    // ==================== SUCCESS ====================

    public static void success(String raw, Stage stage) {
        try {
            BigDecimal newBalance = parseBalance(raw);
            updateBalanceControllers(newBalance);
            ToastUtils.show(stage,"Balance updated!");

        } catch (Exception e) {
            AlertUtils.error("Invalid balance data.");
        }
    }

    // ==================== FAILED ====================
    public static void failed(String data) {
        AlertUtils.error("Balance update failed: " + data);
    }

    // ==================== TRANSACTIONS ====================
    public static void transactions(String data) {
        UserAccountBalanceController controller = ControllerRegistry.get(UserAccountBalanceController.class);
        if (controller != null) {
            controller.updateTransactionList(data);
        }
    }

    // ==================== PARSE ====================
    private static BigDecimal parseBalance(String raw) {
        String balance = raw.replace(SUCCESS_PREFIX, "");
        return new BigDecimal(balance);
    }

    // ==================== UPDATE UI ====================
    private static void updateBalanceControllers(BigDecimal balance) {
        UserAccountBalanceController accountController = ControllerRegistry.get(UserAccountBalanceController.class);
        if (accountController != null) {
            accountController.updateBalanceFromServer(balance);
        }
        UserLiveAuctionController userLiveAuctionController = ControllerRegistry.get(UserLiveAuctionController.class);
        if (userLiveAuctionController != null) {
            userLiveAuctionController.updateBalance(balance);
        }
    }
}
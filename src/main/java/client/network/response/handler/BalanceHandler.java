package client.network.response.handler;

import client.controller.AccountBalanceController;
import client.controller.LiveAuctionController;
import client.util.NavigationUtils;

import javafx.stage.Stage;

import java.math.BigDecimal;

public class BalanceHandler {

    private static final String SUCCESS_PREFIX =
            "BALANCE_SUCCESS|";

    // ==================== SUCCESS ====================

    public static void success(
            String raw,
            Stage stage
    ) {

        try {

            BigDecimal newBalance =
                    parseBalance(raw);

            updateBalanceControllers(
                    newBalance
            );

            NavigationUtils.showInfo(
                    stage,
                    "Balance updated!"
            );

        } catch (Exception e) {

            NavigationUtils.showError(
                    "Invalid balance data."
            );
        }
    }

    // ==================== FAILED ====================

    public static void failed(
            String data,
            Stage stage
    ) {

        NavigationUtils.showError(
                "Balance update failed: " + data
        );
    }

    // ==================== TRANSACTIONS ====================

    public static void transactions(
            String data
    ) {

        AccountBalanceController controller =
                AccountBalanceController.getInstance();

        if (controller != null) {

            controller.updateTransactionList(
                    data
            );
        }
    }

    // ==================== PARSE ====================

    private static BigDecimal parseBalance(
            String raw
    ) {

        String balance =
                raw.replace(
                        SUCCESS_PREFIX,
                        ""
                );

        return new BigDecimal(balance);
    }

    // ==================== UPDATE UI ====================

    private static void updateBalanceControllers(
            BigDecimal balance
    ) {

        AccountBalanceController accountController =
                AccountBalanceController.getInstance();

        if (accountController != null) {

            accountController
                    .updateBalanceFromServer(balance);
        }

        LiveAuctionController liveAuctionController =
                LiveAuctionController.getInstance();

        if (liveAuctionController != null) {

            liveAuctionController
                    .updateBalance(balance);
        }
    }
}
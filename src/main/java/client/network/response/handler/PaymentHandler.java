package client.network.response.handler;

import client.manager.AuctionStateManager;
import client.manager.ControllerRegistry;
import client.controller.ItemViewController;
import client.controller.UserAccountBalanceController;
import client.util.ToastUtils;
import client.util.NavigationUtils;
import javafx.application.Platform;
import model.PaymentStatus;

import java.math.BigDecimal;

public class PaymentHandler {

    public static void paySuccess(String data) {
        // data: auctionId|newBalance
        try {
            String[] parts = data.split("\\|");
            if (parts.length < 2) return;
            String auctionId = parts[0];
            BigDecimal newBalance = new BigDecimal(parts[1]);

            // Defensive trim and logging
            if (auctionId != null) auctionId = auctionId.trim();
            System.out.println("[PaymentHandler] paySuccess for auctionId='" + auctionId + "', newBalance=" + newBalance);
            System.out.println("[PaymentHandler] before: paymentStatus=" + AuctionStateManager.getPayment(auctionId));

            // Mark paid locally
            AuctionStateManager.setPayment(auctionId, PaymentStatus.PAID);
            System.out.println("[PaymentHandler] after: paymentStatus=" + AuctionStateManager.getPayment(auctionId));

            // Update balance UI
            UserAccountBalanceController acc = ControllerRegistry.get(UserAccountBalanceController.class);
            if (acc != null) {
                acc.updateBalanceFromServer(newBalance);
                acc.loadTransactions();
            }

            ItemViewController itemCtrl = ControllerRegistry.get(ItemViewController.class);
            if (itemCtrl != null) {
                Platform.runLater(itemCtrl::refreshState);
            }

            // Toast on main stage if available
            Platform.runLater(() -> {
                try {
                    ToastUtils.show(NavigationUtils.getMainStage(), "Payment successful!");
                } catch (Exception ignored) {}
            });

        } catch (Exception e) {
            System.err.println("PaymentHandler.paySuccess failed: " + e.getMessage());
        }
    }

    public static void payFailed(String data) {
        javafx.application.Platform.runLater(() -> client.util.AlertUtils.error("Payment failed: " + data));
    }
}
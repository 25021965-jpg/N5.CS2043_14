package client.util;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

public final class BalanceTransactionHelper {

    private BalanceTransactionHelper() {
    }

    public static boolean shouldDisplay(
            String selectedFilter,
            String type
    ) {
        if (selectedFilter == null || selectedFilter.equals("All")) {
            return true;
        }

        return switch (selectedFilter) {
            case "Deposit" -> "DEPOSIT".equals(type);
            case "Withdraw" -> "WITHDRAW".equals(type);
            case "Paid" -> "TRANSFER_OUT".equals(type) || "WIN_BID".equals(type);
            case "Received" -> "TRANSFER_IN".equals(type) || "SOLD".equals(type);
            default -> true;
        };
    }

    public static HBox createTransactionCard(
            String type,
            String amount,
            String time,
            String desc
    ) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-radius:12; -fx-border-color:#E2E8F0; -fx-padding:15;");

        VBox left = new VBox(5);
        left.getChildren().addAll(createTitleLabel(type, desc), createTimeLabel(time));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = createAmountLabel(type, amount);
        card.getChildren().addAll(left, spacer, amountLabel);
        return card;
    }

    private static Label createTitleLabel(
            String type,
            String desc
    ) {
        String titleText;

        switch (type) {
            case "DEPOSIT" -> titleText = "Deposit Successful";
            case "WITHDRAW" -> titleText = "Withdraw Successful";
            case "TRANSFER_OUT", "WIN_BID" -> titleText = "Paid to " + desc;
            case "TRANSFER_IN", "SOLD" -> titleText = "Received from " + desc;
            default -> titleText = type;
        }

        Label label = new Label(titleText);
        label.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#0F172A;");
        return label;
    }

    private static Label createTimeLabel(String time) {
        Label label = new Label(time);
        label.setStyle("-fx-text-fill:#64748B; -fx-font-size:13px;");
        return label;
    }

    private static Label createAmountLabel(
            String type,
            String amount
    ) {
        boolean positive = "DEPOSIT".equals(type) || "TRANSFER_IN".equals(type) || "SOLD".equals(type);
        String formatted = amount;
        try {
            BigDecimal value = new BigDecimal(amount);
            String currency = TextUtils.formatCurrency(value);
            if (currency != null && !currency.isBlank()) {
                formatted = currency;
            }
        } catch (Exception ignored) {
        }

        Label label = new Label((positive ? "+ " : "- ") + formatted);
        label.setStyle(positive
                ? "-fx-text-fill:#16A34A; -fx-font-size:18px; -fx-font-weight:bold;"
                : "-fx-text-fill:#EF4444; -fx-font-size:18px; -fx-font-weight:bold;");
        return label;
    }
}

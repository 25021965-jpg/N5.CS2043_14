package client.util;

import java.math.BigDecimal;

public final class LiveAuctionBalanceHelper {
    private static final BigDecimal MAX_DISPLAY_VALUE = new BigDecimal("1000000000");

    private LiveAuctionBalanceHelper() {
    }

    public static double calculateProgress(BigDecimal virtualBalance) {
        if (virtualBalance == null) {
            return 0.0;
        }
        double progress = virtualBalance.doubleValue() / MAX_DISPLAY_VALUE.doubleValue();
        return Math.clamp(progress, 0.0, 1.0);
    }

    public static BigDecimal parseBidAmount(String text) {
        if (text == null) {
            return null;
        }
        try {
            String normalized = text.trim().replace("USD", "").replace(",", "").trim();
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean isValidBidAmount(BigDecimal bidAmount, BigDecimal minRequired, BigDecimal virtualBalance) {
        if (bidAmount == null || minRequired == null || virtualBalance == null) {
            return false;
        }
        return bidAmount.compareTo(minRequired) >= 0 && bidAmount.compareTo(virtualBalance) <= 0;
    }
}

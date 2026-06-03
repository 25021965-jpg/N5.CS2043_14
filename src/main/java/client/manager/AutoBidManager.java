package client.manager;

import java.math.BigDecimal;

public class AutoBidManager {

    private static boolean autoBidEnabled = false;
    private static BigDecimal maxBidAmount = null;
    private static String currentAuctionId = null;

    public static void enable(String auctionId, BigDecimal maxAmount) {
        autoBidEnabled = true;
        maxBidAmount = maxAmount;
        currentAuctionId = auctionId;
    }

    public static void disable() {
        autoBidEnabled = false;
        maxBidAmount = null;
        currentAuctionId = null;
    }

    public static boolean isEnabled() { return autoBidEnabled; }
    public static BigDecimal getMaxAmount() { return maxBidAmount; }
    public static String getAuctionId() { return currentAuctionId; }
}
package client.manager;

import model.Auction;
import model.AuctionStatus;

import java.util.*;

public final class AuctionHistoryManager {

    private static final Set<String> historyAuctionIds = new HashSet<>();
    private static final Map<String, AuctionStatus> statusMap = new HashMap<>();

    private AuctionHistoryManager() {}

    // ==================== HISTORY ====================

    public static void addAuction(String auctionId) {
        if (auctionId != null) {
            historyAuctionIds.add(auctionId);
        }
    }

    public static void addAuctions(List<Auction> auctions) {
        if (auctions == null) return;

        for (Auction auction : auctions) {
            if (auction != null && auction.getAuction_id() != null) {
                historyAuctionIds.add(auction.getAuction_id());
            }
        }
    }

    public static boolean hasHistory(String auctionId) {
        return auctionId != null && historyAuctionIds.contains(auctionId);
    }

    public static Set<String> getHistoryAuctionIds() {
        return new HashSet<>(historyAuctionIds);
    }

    public static void clearHistory() {
        historyAuctionIds.clear();
    }

    // ==================== STATUS ====================

    public static void setStatus(String auctionId, AuctionStatus status) {
        if (auctionId != null && status != null) {
            statusMap.put(auctionId, status);
        }
    }

    public static AuctionStatus getStatus(String auctionId) {
        return statusMap.get(auctionId);
    }

    public static void clearStatus() {
        statusMap.clear();
    }

    // optional: clear all
    public static void clearAll() {
        historyAuctionIds.clear();
        statusMap.clear();
    }
}
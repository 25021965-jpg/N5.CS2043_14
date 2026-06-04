package server.service;

import model.Auction;
import model.AutoBidConfig;
import model.Bid;

import model.Entity.User.User;
import server.dao.BidDAO;
import server.dao.UserDAO;
import server.manager.RoomManager;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBidService {

    // Key: "auctionId:userId" → AutoBidConfig
    private static final Map<String, AutoBidConfig> autoBids = new ConcurrentHashMap<>();

    /** Đăng ký auto-bid cho một người dùng */
    public static String setAutoBid(User user,
                                    Auction auction,
                                    BigDecimal maxAmount) {
        System.out.println("[AutoBid] setAutoBid called - NEW VERSION");


        BigDecimal minRequired =
                auction.getCurrentPrice()
                        .add(auction.getMinIncrement());

        if (maxAmount.compareTo(minRequired) < 0) {
            return "ERROR|Max amount must be at least " + minRequired;
        }

        // Auction must be active to set auto-bid
        if (auction.getStatus() != model.AuctionStatus.ACTIVE) {
            return "ERROR|Cannot enable auto-bid: auction is not active";
        }

        // Check virtual balance (not wallet balance) because bidding uses virtual balance
        java.math.BigDecimal virtual = server.dao.UserDAO.getVirtualBalance(user.getUser_id());
        if (virtual == null) virtual = java.math.BigDecimal.ZERO;
        if (virtual.compareTo(maxAmount) < 0) {
            return "ERROR|Insufficient virtual balance for auto-bid max amount";
        }

        String key =
                buildKey(
                        auction.getAuction_id(),
                        user.getUser_id()
                );

        AutoBidConfig config =
                new AutoBidConfig(
                        user.getUser_id(),
                        auction.getAuction_id(),
                        maxAmount
                );

        autoBids.put(key, config);
        if (isLeading(auction, user.getUser_id())) {
            // Đang leading → không bid thêm
            return "AUTO_BID_SET";
        }
        String result = triggerAutoBid(auction, user.getUser_id());
        // Broadcast TIME_EXTENDED nếu anti-snipe kích hoạt ngay khi enable
            if (result != null && result.contains("EXTENDED")) {
                String[] parts = result.split("\\|");
                if (parts.length >= 4) {
                          RoomManager.broadcastToRoomAll(
                                  auction.getAuction_id(),
                                    "TIME_EXTENDED|" + auction.getAuction_id() + "|" + parts[3]
                          );
                }
            }
            return result;

    }
    /** Hủy auto-bid */
    public static String cancelAutoBid(String auctionId, String userId) {
        String key = buildKey(auctionId, userId);
        AutoBidConfig removed = autoBids.remove(key);
        if (removed != null) {
            System.out.println("[AutoBid] Cancelled: user=" + userId + ", auction=" + auctionId);
            return "AUTO_BID_CANCELLED";
        }
        return "ERROR|No active auto-bid found";
    }

    /**
     * Gọi sau khi có bid mới vào auction.
     * Duyệt tất cả auto-bid đang active cho auction đó,
     * và kích hoạt nếu người đó bị vượt.
     */
    public static void onNewBid(Auction auction, String winnerUserId) {
        String auctionId = auction.getAuction_id();

        autoBids.values().stream()
                .filter(c -> c.getAuctionId().equals(auctionId))
                .filter(c -> !c.getUserId().equals(winnerUserId))
                .filter(c -> !isLeading(auction, c.getUserId()))
                .filter(AutoBidConfig::isActive)
                .sorted(java.util.Comparator.comparingLong(AutoBidConfig::getRegisteredAt)) // ưu tiên bấm trước
                .forEach(c -> triggerAutoBid(auction, c.getUserId()));
    }

    /** Thực hiện auto-bid cho một user cụ thể */
    private static String triggerAutoBid(Auction auction, String userId) {
        BigDecimal latestPrice = BidDAO.getHighestBidAmount(auction.getAuction_id());
        if (latestPrice != null && latestPrice.compareTo(auction.getCurrentPrice()) > 0) {
            auction.setCurrentPrice(latestPrice);
        }
        Bid highest =
                BidDAO.getHighestBid(
                        auction.getAuction_id()
                );

        if (highest != null
                && highest.getBidder() != null
                && highest.getBidder()
                .getUser_id()
                .equals(userId)) {

            return "AUTO_BID_ALREADY_LEADING";
        }
        if (isLeading(auction, userId)) {
            return "AUTO_BID_ALREADY_LEADING";
        }
        String key = buildKey(auction.getAuction_id(), userId);
        AutoBidConfig config = autoBids.get(key);
        if (config == null || !config.isActive()) return "AUTO_BID_SKIPPED";

        BigDecimal nextBid = auction.getCurrentPrice().add(auction.getMinIncrement());

        // Nếu nextBid vượt max → hủy auto-bid, không đặt
        if (nextBid.compareTo(config.getMaxAmount()) > 0) {
            config.setActive(false);
            autoBids.remove(key);
            System.out.println("[AutoBid] Max reached, cancelled for user=" + userId);
            return "AUTO_BID_MAX_REACHED";
        }

        // Lấy user từ DB để kiểm tra balance mới nhất
        User bidder = UserDAO.getUserById(userId);
        if (bidder == null) return "AUTO_BID_USER_NOT_FOUND";

        // Dùng mức giá tối thiểu cần thiết (không bid thẳng max)
        BigDecimal bidAmount = nextBid.min(config.getMaxAmount());

        String result = BidService.placeBid(bidder, auction, bidAmount,true);
        System.out.println("[AutoBid] Triggered: user=" + userId
                + ", amount=" + bidAmount + ", result=" + result);
        if (result.startsWith("BID_SUCCESS")) {
            String broadcastMsg = "UPDATE_PRICE|" + auction.getAuction_id()
                    + "|" + bidAmount
                    + "|[AutoBid]"
                    + "|" + java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
                    + "|" + userId;
            server.manager.RoomManager.broadcastToRoomAll(auction.getAuction_id(), broadcastMsg);

            // Nếu anti-snipe đã extend, broadcast TIME_EXTENDED cho client
            String[] resultParts = result.split("\\|");
            if (resultParts.length >= 4 && resultParts[2].equals("EXTENDED")) {
                String newEndTime = resultParts[3];
                server.manager.RoomManager.broadcastToRoomAll(
                        auction.getAuction_id(),
                        "TIME_EXTENDED|" + auction.getAuction_id() + "|" + newEndTime);
            }
        }
        return result;
    }

    public static boolean hasAutoBid(String auctionId, String userId) {
        AutoBidConfig config = autoBids.get(buildKey(auctionId, userId));
        return config != null && config.isActive();
    }

    public static BigDecimal getAutoBidMax(String auctionId, String userId) {
        AutoBidConfig config = autoBids.get(buildKey(auctionId, userId));
        return config != null ? config.getMaxAmount() : null;
    }

    private static String buildKey(String auctionId, String userId) {
        return auctionId + ":" + userId;
    }
    private static boolean isLeading(
            Auction auction,
            String userId
    ) {

        Bid highest =
                BidDAO.getHighestBid(
                        auction.getAuction_id()
                );

        if (highest == null) {
            System.out.println(
                    "[AutoBid] No highest bid"
            );
            return false;
        }

        if (highest.getBidder() == null) {
            System.out.println(
                    "[AutoBid] Highest bidder null"
            );
            return false;
        }

        String highestUserId =
                highest.getBidder()
                        .getUser_id();

        System.out.println(
                "[AutoBid] Highest="
                        + highestUserId
        );

        System.out.println(
                "[AutoBid] Current="
                        + userId
        );

        return highestUserId.equals(
                userId
        );
    }
}
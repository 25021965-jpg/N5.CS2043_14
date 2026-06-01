package server.service;

import model.*;
import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.dao.UserDAO;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidService {
    public static final int ANTI_SNIPE_WINDOW_SECONDS = 10;   // bid trong 10s cuối
    public static final int ANTI_SNIPE_EXTEND_SECONDS = 60;   // cộng thêm 60s
    public static final int ANTI_SNIPE_MAX_EXTENDS = 5;        // tối đa 5 lần
    public static String placeBid(User bidder, Auction auction, BigDecimal amount) {
        // Synchronized để tránh 2 thằng cùng đặt giá 1 lúc
        synchronized (auction) {

            // Kiểm tra đăng nhập
            if (!bidder.hasRole(Role.BIDDER)) {
                return "ERROR|You do not have permission to place a bid!";
            }

            // Kiểm tra trạng thái đấu giá
            if (auction.getStatus() != AuctionStatus.ACTIVE) {
                return "ERROR|Auction is not active (Status: " + auction.getStatus() + ")";
            }

            // Kiểm tra bước giá
            BigDecimal minRequired = auction.getCurrentPrice().add(auction.getMinIncrement());
            if (amount.compareTo(minRequired) < 0) {
                return "ERROR|Bid amount must be at least " + minRequired;
            }

            // Kiểm tra số dư tài khoản của Bidder
            if (bidder.getBalance().compareTo(amount) < 0) {
                return "ERROR|Insufficient balance. Your balance: " + bidder.getBalance();
            }

            // Insert vào bảng bids + Update current_price ở bảng auctions
            boolean success = BidDAO.placeBid(auction.getAuction_id(), bidder.getUser_id(), amount);

            if (success) {
                auction.setCurrentPrice(amount);

                // ⭐ Kiểm tra anti-snipe
                LocalDateTime newEndTime = checkAntiSnipe(auction);
                if (newEndTime != null) {
                    // Cập nhật endTime trong DB và trong object
                    AuctionDAO.updateEndTime(auction.getAuction_id(), newEndTime);
                    auction.setEndTime(newEndTime);
                    System.out.println("⏰ Anti-snipe! Extended to: " + newEndTime);
                    // Trả về kèm newEndTime để AuctionHandler biết cần broadcast TIME_EXTENDED
                    return "BID_SUCCESS|" + amount + "|EXTENDED|" + newEndTime;
                }

                System.out.println("✓ Bid placed: " + bidder.getUsername() + " bid " + amount);
                return "BID_SUCCESS|" + amount;
            } else {
                return "ERROR|Database error during bidding";
            }
        }
    }
    public static void settleAuction(Auction auction) {
        // Lấy bid cao nhất
        java.math.BigDecimal winAmount = BidDAO.getHighestBidAmount(auction.getAuction_id());
        if (winAmount == null || winAmount.compareTo(java.math.BigDecimal.ZERO) == 0) return;

        // Lấy winner (người có bid cao nhất)
        java.util.List<Bid> bids = BidDAO.getBidsByAuctionId(auction.getAuction_id());
        if (bids == null || bids.isEmpty()) return;

        Bid winBid = bids.get(0); // đã sort DESC theo amount
        User winner = winBid.getBidder();
        if (winner == null) return;

        // Lấy seller
        User seller = auction.getSeller();
        if (seller == null) return;

        // Lấy balance thật từ DB
        User winnerFromDB = UserDAO.getUserById(winner.getUser_id());
        User sellerFromDB = UserDAO.getUserById(seller.getUser_id());
        if (winnerFromDB == null || sellerFromDB == null) return;

        // Trừ tiền winner
        java.math.BigDecimal winnerNewBalance = winnerFromDB.getBalance().subtract(winAmount);
        if (winnerNewBalance.compareTo(java.math.BigDecimal.ZERO) < 0) {
            System.out.println("❌ Winner has insufficient balance!");
            return;
        }
        UserDAO.updateBalance(winner.getUser_id(), winnerNewBalance);
        server.dao.TransactionDAO.addTransaction(winner.getUser_id(), winAmount, "WITHDRAW");

        // Cộng tiền seller
        java.math.BigDecimal sellerNewBalance = sellerFromDB.getBalance().add(winAmount);
        UserDAO.updateBalance(seller.getUser_id(), sellerNewBalance);
        server.dao.TransactionDAO.addTransaction(seller.getUser_id(), winAmount, "DEPOSIT");

        System.out.println("✅ Settled: " + winner.getUser_id() + " paid " + winAmount + " to " + seller.getUser_id());
    }
    public static LocalDateTime checkAntiSnipe(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();

        System.out.println("🔍 checkAntiSnipe: now=" + now + ", endTime=" + endTime + ", extendCount=" + auction.getExtendCount());

        if (endTime == null) {
            System.out.println("❌ endTime is null");
            return null;
        }

        if (auction.getExtendCount() >= ANTI_SNIPE_MAX_EXTENDS) {
            System.out.println("❌ Max extends reached: " + auction.getExtendCount());
            return null;
        }

        long secondsLeft = java.time.Duration.between(now, endTime).toSeconds();
        System.out.println("🔍 secondsLeft=" + secondsLeft + ", window=" + ANTI_SNIPE_WINDOW_SECONDS);

        if (secondsLeft >= 0 && secondsLeft < ANTI_SNIPE_WINDOW_SECONDS) {
            auction.setExtendCount(auction.getExtendCount() + 1);
            LocalDateTime newEndTime = endTime.plusSeconds(ANTI_SNIPE_EXTEND_SECONDS);
            System.out.println("✅ Anti-snipe triggered! newEndTime=" + newEndTime);
            return newEndTime;
        }

        System.out.println("❌ Not in window, no extend");
        return null;
    }
}
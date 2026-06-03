package server.service;

import model.*;
import model.Entity.User.Role;
import model.Entity.User.User;
import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.dao.UserDAO;
import server.exception.AuctionClosedException;
import server.exception.InvalidBidException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidService {
    public static final int ANTI_SNIPE_WINDOW_SECONDS = 10;
    public static final int ANTI_SNIPE_EXTEND_SECONDS = 60;
    public static final int ANTI_SNIPE_MAX_EXTENDS = 5;

    public static String placeBid(User bidder, Auction auction, BigDecimal amount) {
        synchronized (auction) {
            try {
                // Kiểm tra quyền
                if (!bidder.hasRole(Role.BIDDER)) {
                    throw new InvalidBidException("You do not have permission to place a bid!");
                }

                // Kiểm tra trạng thái đấu giá
                if (auction.getStatus() != AuctionStatus.ACTIVE) {
                    throw new AuctionClosedException(
                            "Auction is not active (Status: " + auction.getStatus() + ")"
                    );
                }

                // Kiểm tra bước giá
                BigDecimal minRequired = auction.getCurrentPrice().add(auction.getMinIncrement());
                if (amount.compareTo(minRequired) < 0) {
                    throw new InvalidBidException(
                            "Bid amount must be at least " + minRequired
                    );
                }


                BigDecimal previousBidAmount = BidDAO.getUserMaxBid(auction.getAuction_id(), bidder.getUser_id());
                if (previousBidAmount == null) {
                    previousBidAmount = BigDecimal.ZERO;
                }


                BigDecimal additionalAmount = amount.subtract(previousBidAmount);

                if (additionalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new InvalidBidException("New bid must be higher than your previous bid");
                }


                // KIỂM TRA VIRTUAL BALANCE (KHÔNG PHẢI BALANCE THẬT)
                BigDecimal virtualBal = UserDAO.getVirtualBalance(bidder.getUser_id());
                if (virtualBal == null || virtualBal.compareTo(additionalAmount) < 0) {
                    throw new InvalidBidException("Insufficient balance. Need additional: " + additionalAmount);
                }

                // TRỪ VIRTUAL BALANCE (ATOMIC)
                boolean deducted = UserDAO.deductVirtualBalance(bidder.getUser_id(), additionalAmount);
                if (!deducted) {
                    throw new InvalidBidException("Failed to deduct virtual balance.");
                }

                // Lưu bid vào DB
                boolean success = BidDAO.placeBid(auction.getAuction_id(), bidder.getUser_id(), amount);

                if (success) {
                    auction.setCurrentPrice(amount);

                    // Kiểm tra anti-snipe
                    LocalDateTime newEndTime = checkAntiSnipe(auction);
                    if (newEndTime != null) {
                        AuctionDAO.updateEndTime(auction.getAuction_id(), newEndTime);
                        auction.setEndTime(newEndTime);
                        System.out.println("Anti-snipe! Extended to: " + newEndTime);
                        return "BID_SUCCESS|" + amount + "|EXTENDED|" + newEndTime;
                    }

                    System.out.println("✓ Bid placed: " + bidder.getUsername() + " bid " + amount);
                    return "BID_SUCCESS|" + amount;
                } else {
                    return "ERROR|Database error during bidding";
                }

            } catch (AuctionClosedException | InvalidBidException e) {
                return "ERROR|" + e.getMessage();
            }
        }
    }

    public synchronized void settleAuction(Auction auction) {
        // Lấy bid cao nhất
        java.math.BigDecimal winAmount = BidDAO.getHighestBidAmount(auction.getAuction_id());
        if (winAmount == null || winAmount.compareTo(java.math.BigDecimal.ZERO) == 0) return;

        // Lấy winner (người có bid cao nhất)
        java.util.List<Bid> bids = BidDAO.getBidsByAuctionId(auction.getAuction_id());
        if (bids.isEmpty()) return;

        Bid winBid = bids.getFirst();
        User winner = winBid.getBidder();
        if (winner == null) return;

        User seller = auction.getSeller();
        if (seller == null) return;

        User winnerFromDB = UserDAO.getUserById(winner.getUser_id());
        User sellerFromDB = UserDAO.getUserById(seller.getUser_id());
        if (winnerFromDB == null || sellerFromDB == null) return;

        // Trừ account balance thật của winner (tiền thật)
        BigDecimal winnerNewBalance = winnerFromDB.getBalance().subtract(winAmount);
        if (winnerNewBalance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Winner has insufficient balance!");
            return;
        }
        UserDAO.updateBalance(winner.getUser_id(), winnerNewBalance);

        // Cập nhật lại virtual balance = account balance mới
        UserDAO.updateVirtualBalance(winner.getUser_id(), winnerNewBalance);
        server.dao.TransactionDAO.addTransaction(winner.getUser_id(), winAmount, "WITHDRAW");

        // Cộng tiền cho seller
        BigDecimal sellerNewBalance = sellerFromDB.getBalance().add(winAmount);
        UserDAO.updateBalance(seller.getUser_id(), sellerNewBalance);
        UserDAO.updateVirtualBalance(seller.getUser_id(), sellerNewBalance);
        server.dao.TransactionDAO.addTransaction(seller.getUser_id(), winAmount, "DEPOSIT");

        System.out.println("Settled: " + winner.getUsername()
                + " paid " + winAmount + " to " + seller.getUsername());
    }

    public static LocalDateTime checkAntiSnipe(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();

        System.out.println("checkAntiSnipe: now=" + now + ", endTime=" + endTime
                + ", extendCount=" + auction.getExtendCount());

        if (endTime == null) {
            System.out.println("endTime is null");
            return null;
        }

        if (auction.getExtendCount() >= ANTI_SNIPE_MAX_EXTENDS) {
            System.out.println("Max extends reached: " + auction.getExtendCount());
            return null;
        }

        long secondsLeft = java.time.Duration.between(now, endTime).toSeconds();
        System.out.println("secondsLeft=" + secondsLeft + ", window=" + ANTI_SNIPE_WINDOW_SECONDS);

        if (secondsLeft >= 0 && secondsLeft < ANTI_SNIPE_WINDOW_SECONDS) {
            auction.setExtendCount(auction.getExtendCount() + 1);
            LocalDateTime newEndTime = endTime.plusSeconds(ANTI_SNIPE_EXTEND_SECONDS);
            System.out.println("Anti-snipe triggered! newEndTime=" + newEndTime);
            return newEndTime;
        }

        System.out.println("Not in window, no extend");
        return null;
    }
}
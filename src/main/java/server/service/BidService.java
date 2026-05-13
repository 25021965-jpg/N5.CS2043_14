package server.service;

import model.*;
import server.dao.BidDAO;
import server.dao.UserDAO;
import java.math.BigDecimal;

public class BidService {

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
                // Cập nhật dữ liệu trong bộ nhớ RAM để các Thread khác thấy ngay
                auction.setCurrentPrice(amount);

                // Cập nhật lại số dư tạm thời của user trong session (nếu muốn)
                // bidder.setBalance(bidder.getBalance().subtract(amount));

                System.out.println("✓ Bid placed: " + bidder.getUsername() + " bid " + amount + " on " + auction.getAuction_id());
                return "BID_SUCCESS|" + amount;
            } else {
                return "ERROR|Database error during bidding";
            }
        }
    }
}
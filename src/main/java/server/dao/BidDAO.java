package server.dao;

import model.Bid;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

public class BidDAO {

    public static boolean placeBid(String auctionId, String userId, BigDecimal amount) {

        String sqlBid = "INSERT INTO bids (bid_id, auction_id, bidder_id, bid_amount, bid_time) VALUES (?, ?, ?, ?, NOW())";
        String sqlUpdateAuction = "UPDATE auctions SET current_price = ? WHERE auction_id = ?";

        try (Connection conn = DatabaseService.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Chèn lượt Bid mới
                try (PreparedStatement pstmt = conn.prepareStatement(sqlBid)) {
                    pstmt.setString(1, UUID.randomUUID().toString());
                    pstmt.setString(2, auctionId);
                    pstmt.setString(3, userId);
                    pstmt.setBigDecimal(4, amount);
                    pstmt.executeUpdate();
                }

                // Cập nhật giá cao nhất vào bảng Auctions
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateAuction)) {
                    pstmt.setBigDecimal(1, amount);
                    pstmt.setString(2, auctionId);
                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("✕ placeBid Error: " + e.getMessage());
            return false;
        }
    }

    public static List<Bid> getBidsByAuctionId(String auctionId) {
        List<Bid> bids = new ArrayList<>();

        String sql = "SELECT b.bid_id, b.bid_amount, b.bid_time, u.user_id, u.username, u.fullname " +
                "FROM bids b " +
                "JOIN users u ON b.bidder_id = u.user_id " +
                "WHERE b.auction_id = ? " +
                "ORDER BY b.bid_time ASC, b.bid_amount ASC";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auctionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User bidder = new User();
                    bidder.setUser_id(rs.getString("user_id")); // Đồng bộ với UserDAO
                    bidder.setUsername(rs.getString("username"));
                    bidder.setFullname(rs.getString("fullname"));

                    Bid bid = new Bid();
                    bid.setBidder(bidder);
                    bid.setAmount(rs.getBigDecimal("bid_amount"));

                    Timestamp ts = rs.getTimestamp("bid_time");
                    if (ts != null) bid.setTime(ts.toLocalDateTime());

                    bids.add(bid);
                }
            }
        } catch (SQLException e) {
            System.err.println("✕ getBidsByAuctionId Error: " + e.getMessage());
        }
        return bids;
    }

    public static BigDecimal getHighestBidAmount(String auctionId) {

        String sql = "SELECT MAX(bid_amount) FROM bids WHERE auction_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auctionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal max = rs.getBigDecimal(1);
                    return max != null ? max : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            System.err.println("✕ getHighestBidAmount Error: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
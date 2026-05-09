package server.dao;

import model.Bid;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class BidDAO {

    public static boolean placeBid(String auctionId, String userId, BigDecimal amount) {
        String sql = "INSERT INTO bids (auction_id, user_id, bid_amount, bid_time) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auctionId);
            pstmt.setString(2, userId);
            pstmt.setBigDecimal(3, amount);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in placeBid: " + e.getMessage());
            return false;
        }
    }

    public static List<Bid> getBidsByAuctionId(int auctionId) {
        List<Bid> bids = new ArrayList<>();
        // Join with users table to populate the User object inside Bid
        String sql = "SELECT b.bid_amount, b.bid_time, u.id, u.username, u.fullname " +
                "FROM bids b " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.auction_id = ? " +
                "ORDER BY b.bid_amount DESC";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, auctionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // 1. tạo Bidder
                User bidder = new User();
                bidder.setId(rs.getString("id"));
                bidder.setUsername(rs.getString("username"));
                bidder.setFullname(rs.getString("fullname"));

                // 2. tạo bid và set data
                Bid bid = new Bid();
                bid.setBidder(bidder);
                bid.setAmount(rs.getBigDecimal("bid_amount"));
                bid.setTime(rs.getTimestamp("bid_time").toLocalDateTime());

                bids.add(bid);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getBidsByAuctionId: " + e.getMessage());
        }
        return bids;
    }

    public static BigDecimal getHighestBidAmount(int auctionId) {
        String sql = "SELECT MAX(bid_amount) FROM bids WHERE auction_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, auctionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                BigDecimal max = rs.getBigDecimal(1);
                return max != null ? max : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getHighestBidAmount: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
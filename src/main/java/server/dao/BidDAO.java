package server.dao;

import model.Bid;
import model.Entity.User.Role;
import model.Entity.User.User;
import model.Factory.UserFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Logger;

public class BidDAO {
    private static final Logger LOGGER =
            Logger.getLogger(BidDAO.class.getName());

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
            LOGGER.severe("✕ placeBid Error: " + e.getMessage());
            return false;
        }
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

    public static List<Bid> getBidsByAuctionId(String auctionId) {
        List<Bid> bids = new ArrayList<>();

        String sql = "SELECT b.bid_id, b.bid_amount, b.bid_time, u.user_id, u.username, u.fullname " +
                "FROM bids b " +
                "JOIN users u ON b.bidder_id = u.user_id " +
                "WHERE b.auction_id = ? " +
                "ORDER BY b.bid_amount DESC, b.bid_time ASC";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auctionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User bidder = UserFactory.create(Role.BIDDER);
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
            LOGGER.severe("✕ getBidsByAuctionId Error: " + e.getMessage());
        }
        return bids;
    }

    // ==================== LẤY SỐ TIỀN ĐÃ ĐẶT CAO NHẤT CỦA USER TRONG AUCTION ====================
    /**
     * Lấy số tiền đã đặt cao nhất của một user trong một auction cụ thể
     * @param auctionId ID của phiên đấu giá
     * @param userId ID của người dùng
     * @return Số tiền đã đặt cao nhất, hoặc BigDecimal.ZERO nếu chưa đặt lần nào
     */
    public static BigDecimal getUserMaxBid(String auctionId, String userId) {
        String sql = "SELECT MAX(bid_amount) FROM bids WHERE auction_id = ? AND bidder_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auctionId);
            ps.setString(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal max = rs.getBigDecimal(1);
                return max != null ? max : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            LOGGER.severe("✕ getHighestBidAmount Error: " + e.getMessage());
            System.err.println("✕ getUserMaxBid Error: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
    public static Bid getHighestBid(String auctionId) {

        List<Bid> bids = getBidsByAuctionId(auctionId);

        if (bids == null || bids.isEmpty()) {
            return null;
        }

        return bids.get(0);
    }
}
package server.dao;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    // ==================== MAP AUCTION TỪ RESULTSET ====================
    private static Auction mapAuction(ResultSet rs, Connection conn) throws SQLException {
        Auction auction = new Auction();
        auction.setAuction_id(rs.getString("auction_id"));
        auction.setStartingPrice(rs.getBigDecimal("starting_price"));
        auction.setCurrentPrice(rs.getBigDecimal("current_price"));
        auction.setMinIncrement(rs.getBigDecimal("min_increment"));
        auction.setCancelled(rs.getBoolean("is_cancelled"));
        auction.setApproved(rs.getBoolean("is_approved"));  // ← THÊM

        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) auction.setStartTime(startTs.toLocalDateTime());

        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null) auction.setEndTime(endTs.toLocalDateTime());

        User seller = new User();
        seller.setUser_id(rs.getString("seller_id"));
        seller.setUsername(rs.getString("seller_name"));
        seller.setFullname(rs.getString("seller_fullname"));
        auction.setSeller(seller);

        Item item = new Item();
        item.setItem_id(rs.getString("item_id"));
        item.setName(rs.getString("item_name"));
        item.setDescription(rs.getString("item_desc"));
        String catStr = rs.getString("category");
        item.setCategory(catStr != null ? Category.valueOf(catStr.toUpperCase()) : Category.OTHER);
        item.setImages(getItemImages(conn, item.getItem_id()));
        auction.setItem(item);

        return auction;
    }

    // ==================== FIND ALL (CHỈ ĐÃ DUYỆT) ====================
    public static List<Auction> findAll() {
        List<Auction> auctions = new ArrayList<>();
        String sql = "SELECT a.*, i.name AS item_name, i.description AS item_desc, i.category, " +
                "u.username AS seller_name, u.fullname AS seller_fullname " +
                "FROM auctions a " +
                "LEFT JOIN items i ON a.item_id = i.item_id " +
                "LEFT JOIN users u ON a.seller_id = u.user_id " +
                "WHERE a.is_approved = TRUE AND a.is_cancelled = FALSE";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                auctions.add(mapAuction(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("✕ Lỗi load danh sách: " + e.getMessage());
        }
        return auctions;
    }

    // ==================== FIND PENDING (CHƯA DUYỆT) ====================
    public static List<Auction> findPending() {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT a.*, i.name AS item_name, i.description AS item_desc, i.category, " +
                "u.username AS seller_name, u.fullname AS seller_fullname " +
                "FROM auctions a " +
                "LEFT JOIN items i ON a.item_id = i.item_id " +
                "LEFT JOIN users u ON a.seller_id = u.user_id " +
                "WHERE a.is_approved = FALSE AND a.is_cancelled = FALSE";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapAuction(rs, conn));
            }
        } catch (Exception e) {
            System.err.println("AuctionDAO.findPending error: " + e.getMessage());
        }
        return list;
    }

    // ==================== APPROVE ====================
    public static boolean approveAuction(String auctionId) {
        String sql = "UPDATE auctions SET is_approved = TRUE WHERE auction_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auctionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AuctionDAO.approveAuction error: " + e.getMessage());
            return false;
        }
    }

    // ==================== SAVE ====================
    public static void save(Auction auction) {
        if (auction == null || auction.getItem() == null || auction.getSeller() == null) return;

        String sql = "INSERT INTO auctions " +
                "(auction_id, item_id, seller_id, starting_price, current_price, min_increment, " +
                "start_time, end_time, is_cancelled, is_approved) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection()) {
            conn.setAutoCommit(false);
            try {
                saveItem(conn, auction.getItem());

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, auction.getAuction_id());
                    pstmt.setString(2, auction.getItem().getItem_id());
                    pstmt.setString(3, auction.getSeller().getUser_id());
                    pstmt.setBigDecimal(4, auction.getStartingPrice());
                    pstmt.setBigDecimal(5, auction.getCurrentPrice());
                    pstmt.setBigDecimal(6, auction.getMinIncrement());
                    pstmt.setTimestamp(7, Timestamp.valueOf(auction.getStartTime()));
                    pstmt.setTimestamp(8, Timestamp.valueOf(auction.getEndTime()));
                    pstmt.setBoolean(9, false);
                    pstmt.setBoolean(10, false);
                    pstmt.executeUpdate();
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // ==================== GET BY ID ====================
    public static Auction getAuctionById(String auctionId) {
        String sql = "SELECT a.*, i.name AS item_name, i.description AS item_desc, i.category, " +
                "u.username AS seller_name, u.fullname AS seller_fullname " +
                "FROM auctions a " +
                "LEFT JOIN items i ON a.item_id = i.item_id " +
                "LEFT JOIN users u ON a.seller_id = u.user_id " +
                "WHERE a.auction_id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auctionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapAuction(rs, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Auction> findBySeller(String sellerId) {

        List<Auction> auctions = new ArrayList<>();

        String sql =
                "SELECT a.*, " +
                        "i.name AS item_name, " +
                        "i.description AS item_desc, " +
                        "i.category, " +
                        "u.username AS seller_name, " +
                        "u.fullname AS seller_fullname " +
                        "FROM auctions a " +
                        "LEFT JOIN items i ON a.item_id = i.item_id " +
                        "LEFT JOIN users u ON a.seller_id = u.user_id " +
                        "WHERE a.seller_id = ? " +
                        "ORDER BY a.start_time DESC";

        try (
                Connection conn =
                        DatabaseService.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, sellerId);

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {
                auctions.add(
                        mapAuction(rs, conn)
                );
            }

        } catch (SQLException e) {
            System.err.println(
                    "findBySeller error: "
                            + e.getMessage()
            );
        }

        return auctions;
    }

    // ==================== CANCEL ====================
    public static void cancelAuction(String id) {
        String sql = "UPDATE auctions SET is_cancelled = TRUE WHERE auction_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // ==================== STOP ====================
    public static boolean stopAuction(String id) {
        String sql = "UPDATE auctions SET end_time = NOW() WHERE auction_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AuctionDAO.stopAuction error: " + e.getMessage());
            return false;
        }
    }

    // ==================== RESUME ====================
    public static boolean resumeAuction(String id) {
        String sql = "UPDATE auctions SET is_cancelled = FALSE WHERE auction_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AuctionDAO.resumeAuction error: " + e.getMessage());
            return false;
        }
    }

    // ==================== FIND ALL AS STRINGS (ADMIN) ====================
    public static List<String[]> findAllAsStrings() {
        List<String[]> result = new ArrayList<>();
        String sql = """
            SELECT a.auction_id, i.name AS item_name, u.username AS seller,
                   a.current_price, a.is_approved, a.is_cancelled,
                   a.start_time, a.end_time
            FROM auctions a
            LEFT JOIN items i ON a.item_id = i.item_id
            LEFT JOIN users u ON a.seller_id = u.user_id
            ORDER BY a.start_time DESC
            """;
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Tính status động như Auction.getStatus()
                boolean cancelled = rs.getBoolean("is_cancelled");
                boolean approved  = rs.getBoolean("is_approved");
                java.time.LocalDateTime now   = java.time.LocalDateTime.now();
                java.time.LocalDateTime start = rs.getTimestamp("start_time").toLocalDateTime();
                java.time.LocalDateTime end   = rs.getTimestamp("end_time").toLocalDateTime();

                String status;
                if (cancelled)             status = "CANCELLED";
                else if (!approved)        status = "PENDING_APPROVAL";
                else if (now.isBefore(start)) status = "UPCOMING";
                else if (now.isAfter(end))    status = "ENDED";
                else                          status = "ACTIVE";

                result.add(new String[]{
                        rs.getString("auction_id"),
                        rs.getString("item_name"),
                        rs.getString("seller"),
                        rs.getBigDecimal("current_price").toPlainString(),
                        status
                });
            }
        } catch (SQLException e) {
            System.err.println("AuctionDAO.findAllAsStrings error: " + e.getMessage());
        }
        return result;
    }

    // ==================== AUCTION HISTORY ====================
    public static List<Bid> getBidHistory(String auctionId) {
        List<Bid> history = new ArrayList<>();
        String sql = "SELECT b.bid_amount, b.bid_time, u.username " +
                "FROM bids b JOIN users u ON b.bidder_id = u.user_id " +
                "WHERE b.auction_id = ? ORDER BY b.bid_time ASC";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auctionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Bid bid = new Bid();
                bid.setAmount(rs.getBigDecimal("bid_amount"));
                bid.setTime(rs.getTimestamp("bid_time").toLocalDateTime());
                bid.setUsername(rs.getString("username"));
                history.add(bid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public static List<String[]> findAuctionHistory() {
        List<String[]> result = new ArrayList<>();
        String sql = """
            SELECT a.auction_id, i.name AS item_name,
                   u_winner.username AS winner,
                   a.current_price AS final_bid, a.end_time,
                   a.is_cancelled, a.is_approved, a.start_time
            FROM auctions a
            LEFT JOIN items i ON a.item_id = i.item_id
            LEFT JOIN bids b ON a.auction_id = b.auction_id
                AND b.bid_amount = a.current_price
            LEFT JOIN users u_winner ON b.bidder_id = u_winner.user_id
            WHERE a.is_cancelled = TRUE OR NOW() > a.end_time
            ORDER BY a.end_time DESC
            """;
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                boolean cancelled = rs.getBoolean("is_cancelled");
                String status = cancelled ? "CANCELLED" : "ENDED";
                result.add(new String[]{
                        rs.getString("auction_id"),
                        rs.getString("item_name"),
                        rs.getString("winner") != null ? rs.getString("winner") : "No winner",
                        rs.getBigDecimal("final_bid").toPlainString(),
                        rs.getTimestamp("end_time").toLocalDateTime().toString(),
                        status
                });
            }
        } catch (SQLException e) {
            System.err.println("AuctionDAO.findAuctionHistory error: " + e.getMessage());
        }
        return result;
    }

    public void updateStatus(String auctionId, AuctionStatus status) {
        // Giữ lại để code cũ không lỗi compile — không dùng nữa
        System.out.println("updateStatus() deprecated, dùng approveAuction() thay thế");
    }

    // ==================== PRIVATE HELPERS ====================
    private static void saveItem(Connection conn, Item item) {
        if (item == null || item.getItem_id() == null) return;
        String sql = "INSERT IGNORE INTO items (item_id, name, description, category) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getItem_id());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setString(4, item.getCategory() != null ? item.getCategory().name() : "OTHER");
            pstmt.executeUpdate();
            if (item.getImages() != null) {
                for (String url : item.getImages()) {
                    if (url != null && !url.isBlank()) saveItemImage(conn, item.getItem_id(), url);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void saveItemImage(Connection conn, String itemId, String url) {
        String sql = "INSERT IGNORE INTO item_images (image_id, item_id, image_url) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, java.util.UUID.randomUUID().toString());
            pstmt.setString(2, itemId);
            pstmt.setString(3, url);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static List<String> getItemImages(Connection conn, String itemId) {
        List<String> images = new ArrayList<>();
        String sql = "SELECT image_url FROM item_images WHERE item_id = ? ORDER BY created_at ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) images.add(rs.getString("image_url"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching images: " + e.getMessage());
        }
        return images;
    }
}
package server.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class TiDBRollbackBase {

    protected Connection conn;
    protected List<String> createdUserIds = new ArrayList<>();
    protected List<String> createdItemIds = new ArrayList<>();
    protected List<String> createdAuctionIds = new ArrayList<>();

    protected String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void openTransaction() throws Exception {
        conn = DatabaseService.getConnection();
        conn.setAutoCommit(true);
        createdUserIds.clear();
        createdItemIds.clear();
        createdAuctionIds.clear();
    }

    @AfterEach
    void cleanUp() {
        try {
            if (conn != null && !conn.isClosed()) {
                // 1. Xóa Bids trước để tránh lỗi khóa ngoại (Foreign Key)
                try (var ps = conn.prepareStatement("DELETE FROM bids WHERE auction_id IN (SELECT auction_id FROM auctions) OR bidder_id IN (SELECT user_id FROM users)")) {
                    ps.executeUpdate();
                }

                // 2. Xóa Auctions
                for (String id : createdAuctionIds) {
                    try (var ps = conn.prepareStatement("DELETE FROM auctions WHERE auction_id = ?")) {
                        ps.setString(1, id); ps.executeUpdate();
                    }
                }

                // 3. Xóa Items
                for (String id : createdItemIds) {
                    try (var ps = conn.prepareStatement("DELETE FROM items WHERE item_id = ?")) {
                        ps.setString(1, id); ps.executeUpdate();
                    }
                }

                // 4. Xóa Users
                for (String id : createdUserIds) {
                    try (var ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                        ps.setString(1, id); ps.executeUpdate();
                    }
                }
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Dọn dẹp DB lỗi: " + e.getMessage());
        }
    }

    // --- CÁC HÀM INSERT ĐỂ PHỤC VỤ TEST ---

    protected void insertUser(String id, String username, String email, String password, String role, double balance) throws Exception {
        createdUserIds.add(id);
        String sql = "INSERT INTO users(user_id,fullname,username,email,password,role,balance,verified) VALUES(?,?,?,?,?,?,?,TRUE)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, "Test " + username);
            ps.setString(3, username);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.setString(6, role);
            ps.setBigDecimal(7, java.math.BigDecimal.valueOf(balance));
            ps.executeUpdate();
        }
    }

    protected void insertItem(String itemId, String name, String category) throws Exception {
        createdItemIds.add(itemId);
        String sql = "INSERT INTO items(item_id,name,description,category) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            ps.setString(2, name);
            ps.setString(3, "test-desc");
            ps.setString(4, category);
            ps.executeUpdate();
        }
    }

    protected void insertAuction(String auctionId, String itemId, String sellerId, double price,
                                 boolean approved, boolean cancelled, String start, String end) throws Exception {
        createdAuctionIds.add(auctionId);

        String sql = """
            INSERT INTO auctions(auction_id, item_id, seller_id, starting_price, current_price, 
                                 min_increment, start_time, end_time, is_cancelled, is_approved) 
            VALUES (?, ?, ?, ?, ?, 10, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auctionId);
            ps.setString(2, itemId);
            ps.setString(3, sellerId);
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(price));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(price));
            ps.setString(6, start);
            ps.setString(7, end);
            ps.setBoolean(8, cancelled);
            ps.setBoolean(9, approved);
            ps.executeUpdate();
        }
    }
}
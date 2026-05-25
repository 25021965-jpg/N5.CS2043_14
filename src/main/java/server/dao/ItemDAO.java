package server.dao;

import model.Item;
import model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public static List<String[]> findAllWithSeller() {
        List<String[]> result = new ArrayList<>();
        String sql = """
            SELECT i.item_id, i.name, i.category, u.username AS seller,
                   CASE 
                        WHEN a.is_cancelled = TRUE THEN 'CANCELLED'
                        WHEN a.is_approved = FALSE THEN 'PENDING_APPROVAL'
                        WHEN NOW() < a.start_time THEN 'UPCOMING'
                        WHEN NOW() > a.end_time THEN 'ENDED'
                        ELSE 'ACTIVE' 
                   END AS status
            FROM items i
            LEFT JOIN auctions a ON i.item_id = a.item_id
            LEFT JOIN users u ON a.seller_id = u.user_id
            """;
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{
                        rs.getString("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("seller"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            System.err.println("ItemDAO.findAllWithSeller error: " + e.getMessage());
        }
        return result;
    }

    public static void saveItem(Connection conn, Item item) throws SQLException {
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
        }
    }

    private static void saveItemImage(Connection conn, String itemId, String url) throws SQLException {
        String sql = "INSERT IGNORE INTO item_images (image_id, item_id, image_url) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, java.util.UUID.randomUUID().toString());
            pstmt.setString(2, itemId);
            pstmt.setString(3, url);
            pstmt.executeUpdate();
        }
    }

    public static List<String> getItemImages(Connection conn, String itemId) throws SQLException {
        List<String> images = new ArrayList<>();
        String sql = "SELECT image_url FROM item_images WHERE item_id = ? ORDER BY created_at ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) images.add(rs.getString("image_url"));
            }
        }
        return images;
    }

    public static boolean deleteItem(String itemId) {
        String sql = "DELETE FROM items WHERE item_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ItemDAO.deleteItem error: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateItem(String itemId, String newName, String newDescription, String newCategory) {
        String sql = "UPDATE items SET name=?, description=?, category=? WHERE item_id=?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, newDescription);
            ps.setString(3, newCategory);
            ps.setString(4, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ItemDAO.updateItem error: " + e.getMessage());
            return false;
        }
    }
}
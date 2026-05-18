package server.dao;

import model.Item;
import model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    // Lấy tất cả items kèm tên seller (join với auctions & users)
    public static List<String[]> findAllWithSeller() {
        List<String[]> result = new ArrayList<>();
        String sql = """
            SELECT i.item_id, i.name, i.category, u.username AS seller,
                   CASE WHEN a.is_cancelled = TRUE THEN 'CANCELLED'
                        WHEN NOW() < a.start_time THEN 'UPCOMING'
                        WHEN NOW() > a.end_time THEN 'ENDED'
                        ELSE 'ACTIVE' END AS status
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
            System.err.println("ItemDAO.findAll error: " + e.getMessage());
        }
        return result;
    }

    public static boolean deleteItem(String itemId) {
        // Xóa item → cascade xóa auction, bids, images do FOREIGN KEY ON DELETE CASCADE
        String sql = "DELETE FROM items WHERE item_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ItemDAO.delete error: " + e.getMessage());
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
            System.err.println("ItemDAO.update error: " + e.getMessage());
            return false;
        }
    }
}
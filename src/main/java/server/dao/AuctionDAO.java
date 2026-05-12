package server.dao;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class AuctionDAO {

    public static List<Auction> findAll() {
        List<Auction> auctions = new ArrayList<>();
        String sql = "SELECT a.*, i.name AS item_name, i.description AS item_desc, i.image_path, i.category " +
                "FROM auctions a " +
                "LEFT JOIN items i ON a.item_id = i.id";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getString("id"));
                auction.setCurrentPrice(rs.getBigDecimal("current_price"));
                auction.setMinIncrement(rs.getBigDecimal("min_increment"));

                // Map Enum Status
                String statusStr = rs.getString("status");
                if (statusStr != null) auction.setStatus(AuctionStatus.valueOf(statusStr));

                // Map LocalDateTime
                Timestamp startTs = rs.getTimestamp("start_time");
                if (startTs != null) auction.setStartTime(startTs.toLocalDateTime());

                Timestamp endTs = rs.getTimestamp("end_time");
                if (endTs != null) auction.setEndTime(endTs.toLocalDateTime());

                // --- TẠO ITEM ---
                Item item = new Item();
                item.setId(rs.getString("item_id"));
                item.setName(rs.getString("item_name"));
                item.setDescription(rs.getString("item_desc"));

                // --- ĐỌC CATEGORY TỪ DB (QUAN TRỌNG) ---
                String catStr = rs.getString("category");
                item.setCategory(catStr != null ? Category.valueOf(catStr) : Category.OTHER);

                // Lấy ảnh
                String path = rs.getString("image_path");

                if (path != null) {

                    List<String> images =
                            new ArrayList<>();

                    images.add(path);

                    item.setImages(images);
                }

                auction.setItem(item);
                auctions.add(auction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return auctions;
    }

    public static void save(Auction auction) {

        saveItem(auction.getItem());

        String sql = "INSERT INTO auctions (id, item_id, seller_id, current_price, min_increment, start_time, end_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auction.getId());
            pstmt.setString(2, auction.getItem().getId());
            pstmt.setString(3, auction.getSeller().getId());
            pstmt.setBigDecimal(4, auction.getCurrentPrice());
            pstmt.setBigDecimal(5, auction.getMinIncrement());
            pstmt.setTimestamp(6, Timestamp.valueOf(auction.getStartTime()) );
            pstmt.setTimestamp(7, Timestamp.valueOf(auction.getEndTime()));
            pstmt.setString(8, auction.getStatus().name());


            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("✓ Auction saved successfully: " + auction.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveItem(Item item) {
        if (item == null || item.getId() == null) return;

        String sql = "INSERT IGNORE INTO items (id, name, description, image_path, category) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            // Lưu ảnh đầu tiên vào image_path
            String firstImg = (item.getImages() != null && !item.getImages().isEmpty())
                    ? item.getImages().get(0) : null;
            pstmt.setString(4, firstImg);
            pstmt.setString(5, item.getCategory() != null ? item.getCategory().name() : "OTHER");

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("saveItem error: " + e.getMessage());
        }
    }

    public static void updateStatus(String id, AuctionStatus status) {
        String sql = "UPDATE auctions SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Gán giá trị vào dấu ? thứ nhất (Chuyển Enum thành String)
            pstmt.setString(1, status.name());

            // Gán giá trị vào dấu ? thứ hai
            pstmt.setString(2, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully updated Auction " + id + " status to " + status);
            }
        } catch (SQLException e) {
            System.err.println("Error updating status: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
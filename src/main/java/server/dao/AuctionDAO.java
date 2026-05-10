package server.dao;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class AuctionDAO {

    public static List<Auction> findAll() {
        List<Auction> auctions = new ArrayList<>();
        String sql = "SELECT a.*, i.name AS item_name, i.description AS item_desc, i.image_path " +
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
                Timestamp ts = rs.getTimestamp("end_time");
                if (ts != null) auction.setEndTime(ts.toLocalDateTime());

                // --- TẠO ITEM ---
                Item item = new Item();
                item.setId(rs.getString("item_id"));
                item.setName(rs.getString("item_name"));
                item.setDescription(rs.getString("item_desc"));

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
        String sql = "INSERT INTO auctions (id, item_id, seller_id, current_price, min_increment, end_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auction.getId());
            pstmt.setString(2, auction.getItem().getId());
            pstmt.setString(3, auction.getSeller().getId());
            pstmt.setBigDecimal(4, auction.getCurrentPrice());
            pstmt.setBigDecimal(5, auction.getMinIncrement());
            pstmt.setTimestamp(6, Timestamp.valueOf(auction.getEndTime()));
            pstmt.setString(7, auction.getStatus().name());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStatus(String id, AuctionStatus status) {
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
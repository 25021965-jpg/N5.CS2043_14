package com.yourpackage.dao;

import com.yourpackage.database.Database;
import com.yourpackage.model.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserDAO {

    // Đăng ký tài khoản mới
    public boolean register(String username, String plainPassword) {
        // Mã hóa mật khẩu trước khi lưu
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Nếu username đã tồn tại (UNIQUE constraint)
            if (e.getMessage().contains("UNIQUE")) {
                System.err.println("Username already exists: " + username);
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    // Đăng nhập: trả về User nếu thành công, null nếu thất bại
    public User login(String username, String plainPassword) {
        String sql = "SELECT id, password FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String hashedFromDB = rs.getString("password");
                // Kiểm tra mật khẩu nhập vào với mật khẩu đã mã hóa
                if (BCrypt.checkpw(plainPassword, hashedFromDB)) {
                    int id = rs.getInt("id");
                    return new User(id, username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Sai username hoặc mật khẩu
    }

    // Kiểm tra username đã tồn tại chưa (tuỳ chọn)
    public boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

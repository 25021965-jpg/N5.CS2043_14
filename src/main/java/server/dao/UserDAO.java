package server.dao;

import model.User;
import model.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public class UserDAO {

    // AUTHENTICATION
    public static boolean login(String input, String password) {
        String sql = "SELECT 1 FROM users WHERE (username = ? OR email = ?) AND password = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, input);
            ps.setString(2, input);
            ps.setString(3, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== UPDATE BALANCE ====================
    public static boolean updateBalance(String userId, BigDecimal newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setString(2, userId);
            int rows = ps.executeUpdate();
            System.out.println("[UserDAO] updateBalance rows: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //  THÊM METHOD getUserById NÀY
    public static User getUserById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean register(String fullname, String username, String email, String password, String dob) {
        String sql = "INSERT INTO users(user_id, fullname, username, email, password, dob, balance, verified, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0, true, 'BIDDER')";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, fullname);
            ps.setString(3, username);
            ps.setString(4, email);
            ps.setString(5, password);

            if (dob == null || dob.isEmpty()) ps.setNull(6, Types.DATE);
            else ps.setDate(6, Date.valueOf(dob));

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean resetPassword(String fullname, String dob, String username, String email, String newPassword) {
        String checkSql = "SELECT 1 FROM users WHERE fullname = ? AND dob = ? AND username = ? AND email = ?";
        String updateSql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, fullname);
            // Fix lỗi null date tiềm ẩn
            if (dob == null || dob.isEmpty()) checkPs.setNull(2, Types.DATE);
            else checkPs.setDate(2, Date.valueOf(dob));

            checkPs.setString(3, username);
            checkPs.setString(4, email);

            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next()) return false;
            }

            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setString(1, newPassword);
                updatePs.setString(2, email);
                return updatePs.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // QUERY
    public static List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static User findByUsernameOrEmail(String input) {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, input);
            ps.setString(2, input);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // UPDATE + DELETE
    public static void save(User user) {
        String sql = "INSERT INTO users (user_id, fullname, username, email, password, dob, balance, verified, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE fullname=?, password=?, dob=?, balance=?, verified=?, role=?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Insert params
            pstmt.setString(1, user.getUser_id());
            pstmt.setString(2, user.getFullname());
            pstmt.setString(3, user.getUsername());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPassword());
            if (user.getDob() == null || user.getDob().isEmpty()) pstmt.setNull(6, Types.DATE);
            else pstmt.setDate(6, Date.valueOf(user.getDob()));
            pstmt.setBigDecimal(7, user.getBalance());
            pstmt.setBoolean(8, user.isVerified());
            pstmt.setString(9, user.getRole().name());
            // Update params
            pstmt.setString(10, user.getFullname());
            pstmt.setString(11, user.getPassword());
            if (user.getDob() == null || user.getDob().isEmpty()) pstmt.setNull(12, Types.DATE);
            else pstmt.setDate(12, Date.valueOf(user.getDob()));
            pstmt.setBigDecimal(13, user.getBalance());
            pstmt.setBoolean(14, user.isVerified());
            pstmt.setString(15, user.getRole().name());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public void updateRole(String userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.toUpperCase());
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Helper
    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUser_id(rs.getString("user_id"));
        u.setFullname(rs.getString("fullname"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        Date dobDate = rs.getDate("dob");
        if (dobDate != null) u.setDob(dobDate.toString());
        u.setBalance(rs.getBigDecimal("balance"));
        u.setVerified(rs.getBoolean("verified"));

        String roleStr = rs.getString("role");
        if (roleStr != null) {
            try {
                u.setRole(Role.valueOf(roleStr.toUpperCase()));
            } catch (Exception e) {
                u.setRole(Role.BIDDER);
            }
        } else {
            u.setRole(Role.BIDDER);
        }
        return u;
    }

    public static boolean updatePassword(
            String userId,
            String newPassword
    ) {

        String sql =
                "UPDATE users SET password = ? WHERE user_id = ?";

        try (
                Connection conn =
                        DatabaseService.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, newPassword);
            ps.setString(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updateUserRole(
            String userId,
            String role
    ) {

        String sql =
                "UPDATE users SET role = ? WHERE user_id = ?";

        try (
                Connection conn =
                        DatabaseService.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, role);
            ps.setString(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
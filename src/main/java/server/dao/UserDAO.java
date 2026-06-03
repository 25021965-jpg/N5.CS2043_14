package server.dao;

import model.Entity.User.User;
import model.Factory.UserFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {
    private static final Logger LOGGER =
            Logger.getLogger(UserDAO.class.getName());

    // AUTHENTICATION
    public static boolean login(String input, String password) {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, input);
            ps.setString(2, input);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashedPwd = rs.getString("password");
                    return BCrypt.checkpw(password, hashedPwd);
                }
            }
        } catch (Exception e) { LOGGER.log(Level.SEVERE, "Unexpected error", e); }
        return false;
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
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return false;
        }
    }

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
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
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
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return false;
        }
    }

    public static boolean resetPassword(String fullname, String dob, String username, String email, String newPassword) {
        String checkSql = "SELECT 1 FROM users WHERE fullname = ? AND dob = ? AND username = ? AND email = ?";
        String updateSql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, fullname);
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
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
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
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
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
        } catch (Exception e) { LOGGER.log(Level.SEVERE, "Unexpected error", e); }
        return null;
    }

    // UPDATE + DELETE
    public static void save(User user) {
        String sql = "INSERT INTO users (user_id, fullname, username, email, password, dob, balance, verified, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE fullname=?, password=?, dob=?, balance=?, verified=?, role=?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
            pstmt.setString(10, user.getFullname());
            pstmt.setString(11, user.getPassword());
            if (user.getDob() == null || user.getDob().isEmpty()) pstmt.setNull(12, Types.DATE);
            else pstmt.setDate(12, Date.valueOf(user.getDob()));
            pstmt.setBigDecimal(13, user.getBalance());
            pstmt.setBoolean(14, user.isVerified());
            pstmt.setString(15, user.getRole().name());
            pstmt.executeUpdate();
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unexpected error", e); }
    }

    public static boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unexpected error", e); return false; }
    }

    // ==================== VIRTUAL BALANCE (ATOMIC - AN TOÀN) ====================

    public static BigDecimal getVirtualBalance(String userId) {
        String sql = "SELECT virtual_balance FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal vb = rs.getBigDecimal("virtual_balance");
                return vb != null ? vb : BigDecimal.ZERO;
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unexpected error", e); }
        return BigDecimal.ZERO;
    }

    public static boolean updateVirtualBalance(String userId, BigDecimal amount) {
        String sql = "UPDATE users SET virtual_balance = ? WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unexpected error", e); return false; }
    }

    public static boolean initVirtualBalance(String userId, BigDecimal initialBalance) {
        String sql = "UPDATE users SET virtual_balance = ? WHERE user_id = ? AND (virtual_balance IS NULL OR virtual_balance = 0)";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, initialBalance);
            ps.setString(2, userId);
            int rows = ps.executeUpdate();
            System.out.println("[UserDAO] initVirtualBalance rows: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return false;
        }
    }

    //  ATOMIC DEDUCT - TRÁNH RACE CONDITION
    public static boolean deductVirtualBalance(String userId, BigDecimal amount) {
        String sql = "UPDATE users SET virtual_balance = virtual_balance - ? WHERE user_id = ? AND virtual_balance >= ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setString(2, userId);
            ps.setBigDecimal(3, amount);
            int rows = ps.executeUpdate();
            System.out.println(" deductVirtualBalance: userId=" + userId + ", amount=" + amount + ", rows=" + rows);
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return false;
        }
    }

    //  ATOMIC ADD - AN TOÀN
    public static boolean addVirtualBalance(String userId, BigDecimal amount) {
        String sql = "UPDATE users SET virtual_balance = virtual_balance + ? WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setString(2, userId);
            int rows = ps.executeUpdate();
            System.out.println("[UserDAO] addVirtualBalance rows: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return false;
        }
    }

    public void updateRole(String userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.toUpperCase());
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (Exception e) { LOGGER.log(Level.SEVERE, "Unexpected error", e); }
    }

    // Helper
    private static User map(ResultSet rs) throws SQLException {
        User u = UserFactory.createFromRole(rs.getString("role"));
        u.setUser_id(rs.getString("user_id"));
        u.setFullname(rs.getString("fullname"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        Date dobDate = rs.getDate("dob");
        if (dobDate != null) u.setDob(dobDate.toString());
        u.setBalance(rs.getBigDecimal("balance"));
        u.setVerified(rs.getBoolean("verified"));

        return u;
    }

    public static boolean updatePassword(String userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return false;
        }
    }

    public static boolean updateUserRole(String userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return false;
        }
    }
}
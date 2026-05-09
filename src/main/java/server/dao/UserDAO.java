package server.dao;

import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO {

    // LOGIN
    public static boolean login(String input, String password) {

        String sql =
                "SELECT * FROM users " +
                        "WHERE (username = ? OR email = ?) AND password = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, input);
            ps.setString(2, input);
            ps.setString(3, password.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // REGISTER (FIXED)
    public static boolean register(String fullname, String username,
                                   String email, String password, String dob) {

        String sql =
                "INSERT INTO users(id, fullname, username, email, password, dob, balance, verified) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 0, true)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String id = "U" + UUID.randomUUID().toString().substring(0, 9);

            ps.setString(1, id);
            ps.setString(2, fullname);
            ps.setString(3, username);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.setDate(6, dob == null || dob.isEmpty()
                    ? null
                    : Date.valueOf(dob));

            int rows = ps.executeUpdate();
            System.out.println("REGISTER rows = " + rows);
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //SAVE USER
    public void save(User user) {
        String sql =
                "INSERT INTO users " +
                        "(id, fullname, username, email, password, dob, balance, verified) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getFullname());
            pstmt.setString(3, user.getUsername());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPassword());
            // dob
            pstmt.setString(6, user.getDob());
            //balance
            pstmt.setBigDecimal(
                    7,
                    user.getBalance() == null
                            ? java.math.BigDecimal.ZERO
                            : user.getBalance()
            );            pstmt.setBoolean(8, true);

            int rows = pstmt.executeUpdate();

            if (rows <= 0) {
                throw new SQLException("Insert failed");
            }

            System.out.println("SAVE USER SUCCESS");

        } catch (SQLException e) {
            System.err.println("SAVE ERROR:");
            e.printStackTrace();
        }
    }

    // FIND BY USERNAME OR EMAIL
    public User findByUsernameOrEmail(String input) {

        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, input);
            ps.setString(2, input);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // FIND BY USERNAME
    public User findByUsername(String username) {

        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // FIND BY EMAIL
    public User findByEmail(String email) {

        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // MAP RESULT
    private User map(ResultSet rs) throws SQLException {

        User u = new User();

        u.setId(rs.getString("id"));
        u.setFullname(rs.getString("fullname"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setDob(rs.getString("dob"));
        u.setBalance(rs.getBigDecimal("balance"));
        u.setVerified(rs.getBoolean("verified"));

        return u;
    }

    public List<User> findAll() {

        List<User> list = new ArrayList<>();

        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
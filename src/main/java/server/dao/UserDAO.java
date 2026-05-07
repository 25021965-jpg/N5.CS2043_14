package server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.*;

public class UserDAO {

    // ĐĂNG NHẬP
    public static boolean login(String username, String password) {

        username = username.trim();
        password = password.trim();

        String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {

            System.err.println("Login error: " + e.getMessage());
            return false;
        }
    }

    // ĐĂNG KÝ
    public static boolean register(
            String fullname,
            String username,
            String email,
            String password
    ) {

        fullname = fullname.trim();
        username = username.trim();
        email = email.trim();
        password = password.trim();

        // Kiểm tra username đã tồn tại chưa
        if (isExist("username", username)) {
            return false;
        }

        // Kiểm tra email đã tồn tại chưa
        if (isExist("email", email)) {
            return false;
        }

        String sql =
                "INSERT INTO users(fullname, username, email, password) " +
                        "VALUES (?, ?, ?, ?)";

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, fullname);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, password);

            int rows = pstmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {

            System.err.println("Register error: " + e.getMessage());
            return false;
        }
    }

    // KIỂM TRA TỒN TẠI
    private static boolean isExist(String column, String value) {

        if (
                !column.equals("username") &&
                        !column.equals("email")
        ) {

            return false;
        }

        String sql =
                "SELECT 1 FROM users WHERE " + column + " = ?";

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, value);

            try (ResultSet rs = pstmt.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {

            System.err.println(
                    "Exist check error: " + e.getMessage()
            );

            return false;
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save(User user) {
        String sql = "INSERT INTO users (id, username, email, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> findAll() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));

                userList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error load User: " + e.getMessage());
        }
        return userList;
    }
}
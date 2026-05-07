package server.dao;

import java.sql.*;

public class DatabaseService {

    private static final String URL = "jdbc:mysql://localhost:3306/myapp_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Phuong8ch$"; // SỬA MẬT KHẨU CỦA BẠN

    // Kết nối database
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found!", e);
        }
    }

    // ĐĂNG NHẬP
    public static boolean login(String username, String password) {

        username = username.trim();
        password = password.trim();

        String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // trả về true nếu tìm thấy user

        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
            return false;
        }
    }

    // ĐĂNG KÝ
    public static boolean register(String fullname, String username, String email, String password) {

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

        String sql = "INSERT INTO users (fullname, username, email, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    // Kiểm tra tồn tại
    private static boolean isExist(String column, String value) {

        if (!column.equals("username") &&
                !column.equals("email")) {

            return false;
        }

        String sql = "SELECT 1 FROM users WHERE " + column + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Khởi tạo database và bảng (gọi 1 lần khi chạy app)
    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Tạo database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS myapp_db");

            // Chuyển sang database vừa tạo
            stmt.executeUpdate("USE myapp_db");

            // Tạo bảng users
            String createTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "fullname VARCHAR(100) NOT NULL," +
                    "username VARCHAR(50) UNIQUE NOT NULL," +
                    "email VARCHAR(100) UNIQUE NOT NULL," +
                    "password VARCHAR(255) NOT NULL" +
                    ")";
            stmt.executeUpdate(createTable);

            System.out.println("✓ Database and table initialized successfully!");

        } catch (SQLException e) {
            System.err.println("Init database error: " + e.getMessage());
        }
    }
}
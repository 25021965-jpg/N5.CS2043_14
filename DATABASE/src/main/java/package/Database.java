package com.yourpackage.database;

import java.sql.*;

public class Database {
    // File database sẽ được tạo trong thư mục dự án
    private static final String DB_URL = "jdbc:sqlite:users.db";

    // Khởi tạo bảng users nếu chưa có
    public static void init() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "username TEXT UNIQUE NOT NULL," +
                     "password TEXT NOT NULL" +
                     ")";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Database & table users ready.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Lấy kết nối đến database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}

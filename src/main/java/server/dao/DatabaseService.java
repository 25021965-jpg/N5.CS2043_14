package server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {
    private static final String DB_NAME = "auction_system";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
    private static final String USER = "root";
    private static final String PASSWORD = "Pass102938@";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found!", e);
        }
    }

    public static void initDatabase() {
        String serverURL = "jdbc:mysql://localhost:3306/";
        try (Connection conn = DriverManager.getConnection(serverURL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // 1. Tạo database đồng nhất
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);

            // 2. Tạo bảng Users
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "fullname VARCHAR(100), " +
                    "username VARCHAR(50) UNIQUE, " +
                    "email VARCHAR(100) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "dob VARCHAR(20), " +
                    "balance DECIMAL(15,2) DEFAULT 0, " +
                    "verified BOOLEAN DEFAULT TRUE)");

            // 3. Tạo bảng Items
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS items (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(255), " +
                    "description TEXT, " +
                    "image_path VARCHAR(255), " +
                    "category VARCHAR(50))");

            // 4. Tạo bảng Auctions
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS auctions (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "item_id VARCHAR(50), " +
                    "seller_id VARCHAR(50), " +
                    "current_price DECIMAL(15,2), " +
                    "min_increment DECIMAL(15,2), " +
                    "start_time DATETIME, " +
                    "end_time DATETIME, " +
                    "status VARCHAR(20), " +
                    "FOREIGN KEY (item_id) REFERENCES items(id), " +
                    "FOREIGN KEY (seller_id) REFERENCES users(id))");

            // 5. Tạo bảng Bids
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bids (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "auction_id VARCHAR(50), " +
                    "user_id VARCHAR(50), " +
                    "bid_amount DECIMAL(15,2), " +
                    "bid_time DATETIME, " +
                    "FOREIGN KEY (auction_id) REFERENCES auctions(id))");

            System.out.println("✓ Database & All Tables initialized successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

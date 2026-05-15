package server.dao;

import java.sql.*;

public class DatabaseService {
    // Đọc thông tin từ Environment Variables để bảo mật
    private static final String HOST = System.getenv("TIDB_HOST");
    private static final String USER = System.getenv("TIDB_USER");
    private static final String PASS = System.getenv("TIDB_PASS");
    private static final String DB_NAME = "auction_system";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":4000/" + DB_NAME +
                    "?sslMode=REQUIRED&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found!", e);
        }
    }

    public static void initDatabase() {

        String serverURL =
                "jdbc:mysql://" + HOST + ":4000/test?sslMode=REQUIRED";

        try (Connection conn = DriverManager.getConnection(serverURL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            // 1. Khởi tạo Database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);

            // 2. Bảng Người dùng
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id CHAR(36) PRIMARY KEY,
                    fullname VARCHAR(50) NOT NULL,
                    username VARCHAR(15) UNIQUE NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    dob DATE,
                    balance DECIMAL(15,2) DEFAULT 0,
                    role ENUM('BIDDER', 'SELLER', 'ADMIN') DEFAULT 'SELLER',
                    verified BOOLEAN DEFAULT TRUE,
                    CHECK (username NOT LIKE '% %')
                )""");

            // 3. Bảng Vật phẩm
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS items (
                    item_id CHAR(36) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    description VARCHAR(500),
                    category VARCHAR(50)
                )""");

            // 4. Bảng Hình ảnh Vật phẩm
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS item_images (
                    image_id CHAR(36) PRIMARY KEY,
                    item_id CHAR(36) NOT NULL,
                    image_url TEXT NOT NULL,
                    FOREIGN KEY(item_id) REFERENCES items(item_id) ON DELETE CASCADE
                )""");

            // 5. Bảng Phiên đấu giá
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auctions (
                    auction_id CHAR(36) PRIMARY KEY,
                    item_id CHAR(36) UNIQUE NOT NULL,
                    seller_id CHAR(36) NOT NULL,
                    starting_price DECIMAL(15,2) NOT NULL,
                    current_price DECIMAL(15,2) NOT NULL,
                    min_increment DECIMAL(15,2) NOT NULL,
                    start_time DATETIME NOT NULL,
                    end_time DATETIME NOT NULL,
                    is_cancelled BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY(item_id) REFERENCES items(item_id) ON DELETE CASCADE,
                    FOREIGN KEY(seller_id) REFERENCES users(user_id) ON DELETE CASCADE
                )""");

            // 6. Bảng Lượt đấu giá
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bids (
                    bid_id CHAR(36) PRIMARY KEY,
                    auction_id CHAR(36) NOT NULL,
                    bidder_id CHAR(36) NOT NULL,
                    bid_amount DECIMAL(15,2) NOT NULL,
                    bid_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(auction_id) REFERENCES auctions(auction_id) ON DELETE CASCADE,
                    FOREIGN KEY(bidder_id) REFERENCES users(user_id) ON DELETE CASCADE
                )""");

            // 7. Bảng Yêu thích
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS favourites (
                    user_id CHAR(36),
                    item_id CHAR(36),
                    PRIMARY KEY(user_id, item_id),
                    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY(item_id) REFERENCES items(item_id) ON DELETE CASCADE
                )""");

            // 8. Bảng Giao dịch
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id CHAR(36) PRIMARY KEY,
                    user_id CHAR(36) NOT NULL,
                    amount DECIMAL(15,2) NOT NULL,
                    type ENUM('DEPOSIT', 'WITHDRAW') NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )""");

            // 9. thêm tài khoản Admin
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
            try (ResultSet rs = stmt.executeQuery(checkAdmin)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Nếu chưa có admin nào thì tạo mặc định
                    String insertAdmin = """
                    INSERT INTO users (user_id, fullname, username, email, password, role, verified) 
                    VALUES ('ADM-INIT-001', 'System Admin', 'admin', 'admin@auction.com', 'admin123', 'ADMIN', TRUE)
                    """;
                    stmt.executeUpdate(insertAdmin);
                    System.out.println("✓ Created default admin account: admin/admin123");
                }
            }

            System.out.println("✓ TiDB Cloud: Database and tables initialized successfully");
        } catch (SQLException e) {
            System.err.println("✕ TiDB Initialization Error: " + e.getMessage());
            System.err.println("Error: " + e.getMessage());        }
    }


}
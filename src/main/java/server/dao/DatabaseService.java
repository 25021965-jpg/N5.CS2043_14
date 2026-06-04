package server.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.logging.Logger;

public class DatabaseService {

    private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());
    private static HikariDataSource dataSource;

    private static final String HOST = System.getenv("TIDB_HOST");
    private static final String USER = System.getenv("TIDB_USER");
    private static final String PASS = System.getenv("TIDB_PASS");
    private static final String DB_NAME = "auction_system";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initConnectionPool();
            initDatabase();
        } catch (ClassNotFoundException e) {
            LOGGER.severe("MySQL Driver not found: " + e.getMessage());
        }
    }

    private static void initConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + HOST + ":4000/" + DB_NAME +
                "?sslMode=REQUIRED&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC");
        config.setUsername(USER);
        config.setPassword(PASS);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
        System.out.println("✓ HikariCP Connection Pool initialized");
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Connection pool not initialized!");
        }
        return dataSource.getConnection();
    }

    public static void initDatabase() {
        String serverURL = "jdbc:mysql://" + HOST + ":4000/test?sslMode=REQUIRED";

        try (Connection conn = DriverManager.getConnection(serverURL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);

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
                    verified BOOLEAN DEFAULT TRUE
                )""");

            addVirtualBalanceColumnIfNotExists(stmt);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS items (
                    item_id CHAR(36) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    description VARCHAR(500),
                    category VARCHAR(50)
                )""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS item_images (
                    image_id CHAR(36) PRIMARY KEY,
                    item_id CHAR(36) NOT NULL,
                    image_url TEXT NOT NULL,
                    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
                    FOREIGN KEY(item_id) REFERENCES items(item_id) ON DELETE CASCADE
                )""");

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
                    is_approved BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY(item_id) REFERENCES items(item_id) ON DELETE CASCADE,
                    FOREIGN KEY(seller_id) REFERENCES users(user_id) ON DELETE CASCADE
                )""");

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

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS favourites (
                    user_id CHAR(36),
                    item_id CHAR(36),
                    PRIMARY KEY(user_id, item_id),
                    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY(item_id) REFERENCES items(item_id) ON DELETE CASCADE
                )""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id CHAR(36) PRIMARY KEY,
                    user_id CHAR(36) NOT NULL,
                    amount DECIMAL(15,2) NOT NULL,
                    type ENUM('DEPOSIT', 'WITHDRAW', 'WIN_BID', 'SOLD') NOT NULL,
                    related_user_id CHAR(36),
                    description VARCHAR(255),
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY(related_user_id) REFERENCES users(user_id) ON DELETE SET NULL
                )""");

            String checkAdmin = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
            try (ResultSet rs = stmt.executeQuery(checkAdmin)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String hashedPass = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
                    String insertAdmin = "INSERT INTO users (user_id, fullname, username, email, password, role, verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertAdmin)) {
                        ps.setString(1, "ADM-INIT-001");
                        ps.setString(2, "System Admin");
                        ps.setString(3, "admin");
                        ps.setString(4, "admin@auction.com");
                        ps.setString(5, hashedPass);
                        ps.setString(6, "ADMIN");
                        ps.setBoolean(7, true);
                        ps.executeUpdate();
                        System.out.println("✓ Created default admin account: admin/admin123");
                    }
                }
            }

            System.out.println("✓ TiDB Cloud: Database and tables initialized successfully");

        } catch (SQLException e) {
            LOGGER.severe("✕ TiDB Initialization Error: " + e.getMessage());
        }
    }

    private static void addVirtualBalanceColumnIfNotExists(Statement stmt) {
        try {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = '" + DB_NAME + "' " +
                            "AND TABLE_NAME = 'users' " +
                            "AND COLUMN_NAME = 'virtual_balance'"
            );
            rs.next();
            boolean columnExists = rs.getInt(1) > 0;
            rs.close();

            if (!columnExists) {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN virtual_balance DECIMAL(15,2) DEFAULT 0");
                System.out.println("✓ Added virtual_balance column");
                stmt.executeUpdate("UPDATE users SET virtual_balance = balance");
                System.out.println("✓ Initialized virtual_balance = balance");
            }
        } catch (SQLException e) {
            LOGGER.severe("Could not add virtual_balance: " + e.getMessage());
        }
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("✓ Connection pool closed");
        }
    }
}
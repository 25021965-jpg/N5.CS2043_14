package server.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.UUID;

public abstract class TiDBRollbackBase {

    // Connection thật đến TiDB — chỉ rollback và đóng ở @AfterEach
    private Connection realConn;

    // Proxy connection trả về cho DAO — close() bị vô hiệu hóa
    protected Connection conn;

    private MockedStatic<DatabaseService> dbMock;

    @BeforeEach
    void openTransaction() throws Exception {
        realConn = openTestConnection();
        // Tắt auto-commit — mọi SQL nằm trong transaction chưa commit
        realConn.setAutoCommit(false);

        // Tạo proxy: mọi method đều delegate về realConn, riêng close() bị bỏ qua
        conn = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {

                    if ("close".equals(method.getName())) {
                        return null;
                    }

                    // Chặn commit từ DAO
                    if ("commit".equals(method.getName())) {
                        System.out.println("[TEST] Commit blocked");
                        return null;
                    }

                    if ("isClosed".equals(method.getName())) {
                        return false;
                    }

                    return method.invoke(realConn, args);
                }
        );

        // Mock DatabaseService → luôn trả về proxy, không mở connection mới
        dbMock = Mockito.mockStatic(DatabaseService.class);
        dbMock.when(DatabaseService::getConnection).thenReturn(conn);
    }

    @AfterEach
    void rollbackTransaction() {
        // Đóng mock trước để tránh can thiệp vào quá trình rollback
        if (dbMock != null) {
            try { dbMock.close(); } catch (Exception ignored) {}
        }
        // Rollback và đóng connection thật — đây là nơi duy nhất được đóng
        try {
            if (realConn != null && !realConn.isClosed()) {
                realConn.rollback();
                realConn.setAutoCommit(true);
                realConn.close();
            }
        } catch (Exception e) {
            System.err.println("[TiDBRollbackBase] Rollback failed: " + e.getMessage());
        }
    }

    // ==================== Các hàm helper để insert data test ====================

    protected String generateId(String prefix) {
        // Tạo ID ngẫu nhiên để tránh xung đột khi chạy song song hoặc chạy lại
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected void insertUser(String id, String username, String email,
                              String password, String role, double balance) throws Exception {
        try (var ps = conn.prepareStatement(
                "INSERT INTO users(user_id,fullname,username,email,password,role,balance,verified) " +
                        "VALUES(?,?,?,?,?,?,?,TRUE)")) {
            ps.setString(1, id);
            ps.setString(2, "Test " + username);
            ps.setString(3, username);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.setString(6, role);
            ps.setBigDecimal(7, java.math.BigDecimal.valueOf(balance));
            ps.executeUpdate();
        }
    }

    protected void insertItem(String itemId, String name, String category) throws Exception {
        try (var ps = conn.prepareStatement(
                "INSERT INTO items(item_id,name,description,category) VALUES(?,?,?,?)")) {
            ps.setString(1, itemId);
            ps.setString(2, name);
            ps.setString(3, "test-desc");
            ps.setString(4, category);
            ps.executeUpdate();
        }
    }

    protected void insertAuction(String auctionId, String itemId, String sellerId,
                                 double price, boolean approved, boolean cancelled,
                                 String start, String end) throws Exception {
        try (var ps = conn.prepareStatement(
                "INSERT INTO auctions(auction_id,item_id,seller_id,starting_price,current_price," +
                        "min_increment,start_time,end_time,is_cancelled,is_approved) VALUES(?,?,?,?,?,10,?,?,?,?)")) {
            ps.setString(1, auctionId);
            ps.setString(2, itemId);
            ps.setString(3, sellerId);
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(price));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(price));
            ps.setString(6, start);
            ps.setString(7, end);
            ps.setBoolean(8, cancelled);
            ps.setBoolean(9, approved);
            ps.executeUpdate();
        }
    }

    // ==================== Kết nối đến TiDB (đọc từ env hoặc file config) ====================

    private static Connection openTestConnection() throws Exception {
        String host = System.getenv("TIDB_HOST");
        String user = System.getenv("TIDB_USER");
        String pass = System.getenv("TIDB_PASS");

        // Nếu biến môi trường chưa set thì đọc từ file db-test.properties
        if (host == null || host.isBlank()) {
            Properties props = new Properties();
            try (InputStream is = TiDBRollbackBase.class
                    .getClassLoader()
                    .getResourceAsStream("db-test.properties")) {
                if (is == null) throw new IllegalStateException(
                        "Missing TIDB_HOST env var and src/test/resources/db-test.properties not found.\n" +
                                "Create the file with:\n  tidb.host=...\n  tidb.user=...\n  tidb.pass=...");
                props.load(is);
            }
            host = props.getProperty("tidb.host");
            user = props.getProperty("tidb.user");
            pass = props.getProperty("tidb.pass");
        }

        String url = "jdbc:mysql://" + host + ":4000/auction_system" +
                "?sslMode=REQUIRED&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, pass);
    }
}
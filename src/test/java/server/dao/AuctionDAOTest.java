package server.dao;

import model.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuctionDAOTest {
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private static String testId;

    @BeforeAll
    static void setup() {
        // 1. Tạo ID ngẫu nhiên cho cuộc đấu giá
        testId = "T_AUC_" + UUID.randomUUID().toString().substring(0, 8);

        // 2. TỰ ĐỘNG CHUẨN BỊ DỮ LIỆU TEST
        System.out.println(">>> Đang kiểm tra và chuẩn bị dữ liệu test (users, items)...");
        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement()) {

            // Chèn user mẫu nếu chưa tồn tại
            stmt.executeUpdate("INSERT IGNORE INTO users (user_id, username, password, fullname) " +
                    "VALUES ('user01', 'test_seller', '123', 'Seller Test')");

            // Chèn item mẫu nếu chưa tồn tại
            stmt.executeUpdate("INSERT IGNORE INTO items (item_id, name, description) " +
                    "VALUES ('item01', 'Sản phẩm Test', 'Mô tả test tự động')");

            System.out.println(">>> Dữ liệu test đã sẵn sàng.");
        } catch (SQLException e) {
            System.err.println("Lỗi chuẩn bị dữ liệu: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test Save: Lưu đấu giá mới vào DB")
    void testSaveAuction() {
        Item mockItem = new Item();
        mockItem.setItem_id("item01");

        User mockSeller = new User();
        mockSeller.setUser_id("user01");

        Auction auction = new Auction();
        auction.setAuction_id(testId);
        auction.setItem(mockItem);
        auction.setSeller(mockSeller);
        auction.setCurrentPrice(new BigDecimal("1500.00"));
        auction.setMinIncrement(new BigDecimal("50.00"));
        auction.setEndTime(LocalDateTime.now().plusDays(1));
        auction.setStatus(AuctionStatus.ACTIVE);

        System.out.println(">>> Lưu Auction ID: " + testId);
        assertDoesNotThrow(() -> AuctionDAO.save(auction));
    }

    @Test
    @Order(2)
    @DisplayName("Test FindAll: Tìm kiếm đấu giá vừa lưu")
    void testFindAll() {
        List<Auction> list = AuctionDAO.findAll();
        assertNotNull(list);

        // Debug: In ra để kiểm tra nếu cần
        System.out.println(">>> Số lượng đấu giá hiện tại: " + list.size());

        boolean found = list.stream().anyMatch(a -> a.getAuction_id().equals(testId));
        assertTrue(found, "Lỗi: Không tìm thấy ID " + testId + " trong Database!");
    }

    @Test
    @Order(3)
    @DisplayName("Test UpdateStatus: Thay đổi trạng thái đấu giá")
    void testUpdateStatus() {
        // Cập nhật từ ACTIVE -> ENDED
        auctionDAO.updateStatus(testId, AuctionStatus.ENDED);

        // Kiểm tra lại kết quả
        List<Auction> list = AuctionDAO.findAll();
        Auction updated = list.stream()
                .filter(a -> a.getAuction_id().equals(testId))
                .findFirst()
                .orElse(null);

        assertNotNull(updated, "Bản ghi vừa update biến mất khỏi DB!");
        assertEquals(AuctionStatus.ENDED, updated.getStatus());
        System.out.println(">>> Cập nhật trạng thái thành công.");
    }

    @AfterAll
    static void tearDown() {
        // DỌN DẸP DỮ LIỆU RÁC (Chỉ xóa bản ghi Test)
        System.out.println(">>> Đang dọn dẹp dữ liệu Test (" + testId + ")...");
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM auctions WHERE auction_id = ?")) {
            pstmt.setString(1, testId);
            pstmt.executeUpdate();
            System.out.println(">>> Database sạch sẽ.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
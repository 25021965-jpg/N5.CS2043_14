package server.dao;

import model.Bid;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.List;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BidDAOTest {

    private static final String T_AUC_ID = "T_AUC_999";
    private static final String T_USR_ID = "user01";

    @Test
    @Order(1)
    @DisplayName("Test Place Bid")
    void testPlaceBid() {
        BigDecimal amount = new BigDecimal("9999.00");
        boolean result = BidDAO.placeBid(T_AUC_ID, T_USR_ID, amount);
    }

    @Test
    @Order(2)
    @DisplayName("Test Highest Bid")
    void testGetHighestBid() {
        BigDecimal highest = BidDAO.getHighestBidAmount(999);
        assertNotNull(highest);
    }

    @Test
    @Order(3)
    @DisplayName("Test Get Bids History")
    void testGetBidsByAuctionId() {
        List<Bid> bids = BidDAO.getBidsByAuctionId(999);
        assertNotNull(bids);
    }

    @AfterAll
    static void cleanUp() {
        System.out.println(">>> CLEANUP: Đang quét sạch rác bảng Bids...");
        String sql = "DELETE FROM bids WHERE auction_id LIKE 'T_%' OR user_id LIKE 'T_%'";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int deleted = pstmt.executeUpdate();
            System.out.println(">>> Đã dọn dẹp " + deleted + " dòng thầu giả.");
        } catch (SQLException e) {
            System.err.println("Cleanup Error: " + e.getMessage());
        }
    }
}
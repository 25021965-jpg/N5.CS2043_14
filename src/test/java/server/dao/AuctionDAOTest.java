package server.dao;

import model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuctionDAOTest extends TiDBRollbackBase {

    // Tạo dữ liệu nền dùng chung cho nhiều test
    private void insertBaseData() throws Exception {
        insertUser("seller-t1", "tseller", "tseller@mail.com", "pass", "SELLER", 0);
        insertItem("item-t1", "TestWatch", "ELECTRONICS");
        insertItem("item-t2", "TestRing", "JEWELRY");
    }

    // ==================== calculateStatus — không cần DB, test pure logic ====================

    @Test
    void testCalculateStatus_AllBranches() {
        LocalDateTime now = LocalDateTime.now();

        // Nhánh: đã bị huỷ
        assertEquals(AuctionStatus.CANCELLED,
                AuctionDAO.calculateStatus(true, true, now, now));

        // Nhánh: chưa được duyệt
        assertEquals(AuctionStatus.PENDING_APPROVAL,
                AuctionDAO.calculateStatus(false, false, now, now));

        // Nhánh: đã duyệt nhưng chưa đến giờ bắt đầu
        assertEquals(AuctionStatus.UPCOMING,
                AuctionDAO.calculateStatus(false, true, now.plusHours(1), now.plusHours(2)));

        // Nhánh: đã qua giờ kết thúc
        assertEquals(AuctionStatus.ENDED,
                AuctionDAO.calculateStatus(false, true, now.minusHours(2), now.minusHours(1)));

        // Nhánh: đang diễn ra
        assertEquals(AuctionStatus.ACTIVE,
                AuctionDAO.calculateStatus(false, true, now.minusHours(1), now.plusHours(1)));

        // Nhánh: thời gian null → không crash, coi như active
        assertEquals(AuctionStatus.ACTIVE,
                AuctionDAO.calculateStatus(false, true, null, null));
    }

    // ==================== findAll, findPending, findBySeller ====================

    @Test
    void testFindQueries() throws Exception {
        insertBaseData();

        // Auction đã duyệt → xuất hiện trong findAll
        insertAuction("auc-1", "item-t1", "seller-t1", 100, true, false,
                "2024-01-01 00:00:00", "2099-01-01 00:00:00");

        // Auction chờ duyệt → xuất hiện trong findPending
        insertAuction("auc-2", "item-t2", "seller-t1", 200, false, false,
                "2024-01-01 00:00:00", "2099-01-01 00:00:00");

        // Kiểm tra findAll chỉ trả về auction đã duyệt
        assertTrue(AuctionDAO.findAll().stream().anyMatch(a -> a.getAuction_id().equals("auc-1")));

        // Kiểm tra findPending chỉ trả về auction chờ duyệt
        assertTrue(AuctionDAO.findPending().stream().anyMatch(a -> a.getAuction_id().equals("auc-2")));

        // Kiểm tra findBySeller trả về đúng số lượng
        assertEquals(2, AuctionDAO.findBySeller("seller-t1").size());
    }

    // ==================== save — bao gồm saveItem và saveItemImage ====================

    @Test
    void testSave_WithImages() throws Exception {
        insertUser("seller-save", "ssave", "ssave@mail.com", "pass", "SELLER", 0);

        // Tạo item kèm ảnh để phủ nhánh saveItemImage
        Item item = new Item();
        item.setItem_id("item-save");
        item.setName("Laptop");
        item.setCategory(Category.ELECTRONICS);
        item.setDescription("Desc");
        List<String> imgs = new ArrayList<>();
        imgs.add("url1");
        imgs.add("url2");
        item.setImages(imgs);

        Auction a = new Auction();
        a.setAuction_id("auc-save");
        a.setItem(item);
        User seller = new User();
        seller.setUser_id("seller-save");
        a.setSeller(seller);
        a.setStartingPrice(new BigDecimal("1000"));
        a.setCurrentPrice(new BigDecimal("1000"));
        a.setMinIncrement(new BigDecimal("50"));
        a.setStartTime(LocalDateTime.now());
        a.setEndTime(LocalDateTime.now().plusDays(1));

        assertDoesNotThrow(() -> AuctionDAO.save(a));

        // Xác nhận ảnh được lưu và đọc lại đúng
        List<Auction> check = AuctionDAO.findBySeller("seller-save");
        assertEquals(2, check.get(0).getItem().getImages().size());
    }

    // ==================== findJoinedAuctions & getBidHistory ====================

    @Test
    void testBidsRelatedQueries() throws Exception {
        insertBaseData();
        insertUser("bidder-1", "tbidder", "bidder@mail.com", "pass", "BIDDER", 1000);
        insertAuction("auc-bid", "item-t1", "seller-t1", 100, true, false,
                "2024-01-01 00:00:00", "2099-01-01 00:00:00");

        // Chèn bid thủ công để các hàm JOIN có dữ liệu
        try (var ps = conn.prepareStatement(
                "INSERT INTO bids(bid_id,auction_id,bidder_id,bid_amount,bid_time) VALUES(?,?,?,?,NOW())")) {
            ps.setString(1, "b-1");
            ps.setString(2, "auc-bid");
            ps.setString(3, "bidder-1");
            ps.setBigDecimal(4, new BigDecimal("150"));
            ps.executeUpdate();
        }

        // Kiểm tra bidder nhìn thấy auction mình đã tham gia
        assertFalse(AuctionDAO.findJoinedAuctions("bidder-1").isEmpty());

        // Kiểm tra lịch sử đấu giá của auction
        assertEquals(1, AuctionDAO.getBidHistory("auc-bid").size());
    }

    // ==================== Approve, Cancel, Stop, Resume ====================

    @Test
    void testStatusUpdates() throws Exception {
        insertBaseData();
        insertAuction("auc-upd", "item-t1", "seller-t1", 100, false, false,
                "2024-01-01 00:00:00", "2099-01-01 00:00:00");

        // Kiểm tra từng bước thay đổi trạng thái
        assertTrue(AuctionDAO.approveAuction("auc-upd"));   // chờ → đã duyệt
        AuctionDAO.cancelAuction("auc-upd");                 // hủy
        assertTrue(AuctionDAO.resumeAuction("auc-upd"));    // khôi phục
        assertTrue(AuctionDAO.stopAuction("auc-upd"));      // dừng sớm
    }

    // ==================== findAuctionHistory & findAllAsStrings ====================

    @Test
    void testHistoryAndStrings() throws Exception {
        insertBaseData();

        // Auction đã kết thúc và bị hủy → xuất hiện trong lịch sử
        insertAuction("auc-h1", "item-t1", "seller-t1", 100, true, true,
                "2020-01-01 00:00:00", "2021-01-01 00:00:00");

        assertFalse(AuctionDAO.findAuctionHistory().isEmpty());
        assertFalse(AuctionDAO.findAllAsStrings().isEmpty());
    }

    // ==================== Kiểm tra các trường hợp null ====================

    @Test
    void testNullChecks() {
        // Truyền null → không crash, return sớm
        assertDoesNotThrow(() -> AuctionDAO.save(null));

        // Auction không có item → không crash
        Auction a = new Auction();
        a.setItem(null);
        assertDoesNotThrow(() -> AuctionDAO.save(a));

        // ID không tồn tại → trả về false
        assertFalse(AuctionDAO.approveAuction("non-exist"));
        assertFalse(AuctionDAO.stopAuction("non-exist"));
        assertFalse(AuctionDAO.resumeAuction("non-exist"));
    }
}
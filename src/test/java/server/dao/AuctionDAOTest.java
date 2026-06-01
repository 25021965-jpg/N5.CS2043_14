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

    // Helper chèn data sạch để test các hàm tìm kiếm
    private void insertBaseData() throws Exception {
        insertUser("seller-t1", "tseller", "tseller@mail.com", "pass", "SELLER", 0);
        insertItem("item-t1", "TestWatch", "ELECTRONICS");
        insertItem("item-t2", "TestRing", "JEWELRY");
    }

    // ==================== 1. PHỦ calculateStatus (100% logic) ====================
    @Test
    void testCalculateStatus_AllBranches() {
        LocalDateTime now = LocalDateTime.now();
        // Nhánh cancelled
        assertEquals(AuctionStatus.CANCELLED, AuctionDAO.calculateStatus(true, true, now, now));
        // Nhánh !approved
        assertEquals(AuctionStatus.PENDING_APPROVAL, AuctionDAO.calculateStatus(false, false, now, now));
        // Nhánh upcoming
        assertEquals(AuctionStatus.UPCOMING, AuctionDAO.calculateStatus(false, true, now.plusHours(1), now.plusHours(2)));
        // Nhánh ended
        assertEquals(AuctionStatus.ENDED, AuctionDAO.calculateStatus(false, true, now.minusHours(2), now.minusHours(1)));
        // Nhánh active
        assertEquals(AuctionStatus.ACTIVE, AuctionDAO.calculateStatus(false, true, now.minusHours(1), now.plusHours(1)));
        // Nhánh null dates (để đảm bảo không crash)
        assertEquals(AuctionStatus.ACTIVE, AuctionDAO.calculateStatus(false, true, null, null));
    }

    // ==================== 2. PHỦ findAll, findPending, findBySeller ====================
    @Test
    void testFindQueries_And_MapAuction() throws Exception {
        insertBaseData();
        // Auction 1: Đã duyệt (hiện ở findAll)
        insertAuction("auc-1", "item-t1", "seller-t1", 100, true, false, "2024-01-01 00:00:00", "2099-01-01 00:00:00");
        // Auction 2: Đang chờ (hiện ở findPending)
        insertAuction("auc-2", "item-t2", "seller-t1", 200, false, false, "2024-01-01 00:00:00", "2099-01-01 00:00:00");

        // Test findAll
        List<Auction> all = AuctionDAO.findAll();
        assertTrue(all.stream().anyMatch(a -> a.getAuction_id().equals("auc-1")));

        // Test findPending
        List<Auction> pending = AuctionDAO.findPending();
        assertTrue(pending.stream().anyMatch(a -> a.getAuction_id().equals("auc-2")));

        // Test findBySeller
        List<Auction> sellerList = AuctionDAO.findBySeller("seller-t1");
        assertEquals(2, sellerList.size());
    }

    // ==================== 3. PHỦ save (Gồm cả saveItem, saveItemImage) ====================
    @Test
    void testSave_WithFullData_And_Images() throws Exception {
        insertUser("seller-save", "ssave", "save@mail.com", "pass", "SELLER", 0);

        Item item = new Item();
        item.setItem_id("item-save");
        item.setName("Laptop");
        item.setCategory(Category.ELECTRONICS);
        item.setDescription("Desc");
        // Thêm ảnh để phủ hàm saveItemImage (private)
        List<String> imgs = new ArrayList<>();
        imgs.add("url1"); imgs.add("url2");
        item.setImages(imgs);

        Auction a = new Auction();
        a.setAuction_id("auc-save");
        a.setItem(item);
        a.setSeller(new User()); a.getSeller().setUser_id("seller-save");
        a.setStartingPrice(new BigDecimal("1000"));
        a.setCurrentPrice(new BigDecimal("1000"));
        a.setMinIncrement(new BigDecimal("50"));
        a.setStartTime(LocalDateTime.now());
        a.setEndTime(LocalDateTime.now().plusDays(1));

        // Lưu vào DB
        assertDoesNotThrow(() -> AuctionDAO.save(a));
        createdAuctionIds.add("auc-save");
        createdItemIds.add("item-save");

        // Kiểm tra xem getItemImages có chạy và map đúng không
        List<Auction> check = AuctionDAO.findBySeller("seller-save");
        Auction saved = check.get(0);
        assertEquals(2, saved.getItem().getImages().size());
    }

    // ==================== 4. PHỦ findJoinedAuctions & getBidHistory ====================
    @Test
    void testBidsRelatedQueries() throws Exception {
        insertBaseData();
        insertUser("bidder-1", "tbidder", "bidder@mail.com", "pass", "BIDDER", 1000);
        insertAuction("auc-bid", "item-t1", "seller-t1", 100, true, false, "2024-01-01 00:00:00", "2099-01-01 00:00:00");

        // Chèn Bid thủ công để các hàm JOIN có dữ liệu trả về
        try (var ps = conn.prepareStatement("INSERT INTO bids(bid_id, auction_id, bidder_id, bid_amount, bid_time) VALUES(?,?,?,?,NOW())")) {
            ps.setString(1, "b-1"); ps.setString(2, "auc-bid"); ps.setString(3, "bidder-1");
            ps.setBigDecimal(4, new BigDecimal("150")); ps.executeUpdate();
        }

        assertFalse(AuctionDAO.findJoinedAuctions("bidder-1").isEmpty());
        assertEquals(1, AuctionDAO.getBidHistory("auc-bid").size());
    }

    // ==================== 5. PHỦ CÁC HÀM UPDATE (Approve, Cancel, Stop, Resume) ====================
    @Test
    void testStatusUpdates() throws Exception {
        insertBaseData();
        insertAuction("auc-upd", "item-t1", "seller-t1", 100, false, false, "2024-01-01 00:00:00", "2099-01-01 00:00:00");

        assertTrue(AuctionDAO.approveAuction("auc-upd"));
        AuctionDAO.cancelAuction("auc-upd");
        assertTrue(AuctionDAO.resumeAuction("auc-upd"));
        assertTrue(AuctionDAO.stopAuction("auc-upd"));

        // Phủ hàm updateAuctionStatus cuối file
        assertDoesNotThrow(() -> AuctionDAO.updateAuctionStatus("auc-upd", "ACTIVE"));
    }

    // ==================== 6. PHỦ findAuctionHistory & findAllAsStrings ====================
    @Test
    void testHistoryAndStrings() throws Exception {
        insertBaseData();
        // 1 cái đã end, 1 cái bị cancel để phủ hết WHERE
        insertAuction("auc-h1", "item-t1", "seller-t1", 100, true, true, "2020-01-01 00:00:00", "2021-01-01 00:00:00");

        assertFalse(AuctionDAO.findAuctionHistory().isEmpty());
        assertFalse(AuctionDAO.findAllAsStrings().isEmpty());
    }

    // ==================== 7. PHỦ CÁC NHÁNH NULL (Corner Cases) ====================
    @Test
    void testNullChecks() {
        assertDoesNotThrow(() -> AuctionDAO.save(null));
        Auction a = new Auction();
        a.setItem(null);
        assertDoesNotThrow(() -> AuctionDAO.save(a)); // Phủ nhánh auction.getItem() == null

        // Phủ các hàm với ID không tồn tại
        assertFalse(AuctionDAO.approveAuction("non-exist"));
        assertFalse(AuctionDAO.stopAuction("non-exist"));
        assertFalse(AuctionDAO.resumeAuction("non-exist"));
    }
}
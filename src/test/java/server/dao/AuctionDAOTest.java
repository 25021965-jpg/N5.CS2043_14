package server.dao;

import model.*;
import model.Entity.Item.*;
import model.Entity.User.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuctionDAOTest extends TiDBRollbackBase {

    private void insertBaseData() throws Exception {
        insertUser("seller-t1", "tseller", "tseller@mail.com", "pass", "SELLER", 0);
        insertItem("item-t1", "TestWatch", "ELECTRONICS");
        insertItem("item-t2", "TestRing", "JEWELRY");
    }

    @Test
    void testCalculateStatus() {
        LocalDateTime now = LocalDateTime.now();
        assertEquals(AuctionStatus.CANCELLED, AuctionDAO.calculateStatus(true, true, now, now));
        assertEquals(AuctionStatus.PENDING_APPROVAL, AuctionDAO.calculateStatus(false, false, now, now));
        assertEquals(AuctionStatus.UPCOMING, AuctionDAO.calculateStatus(false, true, now.plusHours(1), now.plusHours(2)));
        assertEquals(AuctionStatus.ENDED, AuctionDAO.calculateStatus(false, true, now.minusHours(2), now.minusHours(1)));
        assertEquals(AuctionStatus.ACTIVE, AuctionDAO.calculateStatus(false, true, now.minusHours(1), now.plusHours(1)));
        assertEquals(AuctionStatus.ACTIVE, AuctionDAO.calculateStatus(false, true, null, null));
    }

    @Test
    void testFindQueriesAndFlow() throws Exception {
        insertBaseData();
        insertAuction("auc-1", "item-t1", "seller-t1", 100, true, false, "2024-01-01 00:00:00", "2099-01-01 00:00:00");
        insertAuction("auc-2", "item-t2", "seller-t1", 200, false, false, "2024-01-01 00:00:00", "2099-01-01 00:00:00");

        assertFalse(AuctionDAO.findAll().isEmpty());
        assertFalse(AuctionDAO.findPending().isEmpty());
        assertEquals(2, AuctionDAO.findBySeller("seller-t1").size());

        Auction a = AuctionDAO.findById("auc-1");
        assertNotNull(a);
        assertEquals(0, new BigDecimal("100").compareTo(a.getStartingPrice()));
    }

    @Test
    void testSaveAndHistory() throws Exception {
        insertUser("seller-save", "ssave", "ssave@mail.com", "pass", "SELLER", 0);
        Item item = new Electronics();
        item.setItem_id("item-save");
        item.setName("Laptop");
        item.setCategory(Category.ELECTRONICS);
        item.setDescription("Desc");
        item.setImages(new ArrayList<>(List.of("url1")));

        Auction a = new Auction();
        a.setAuction_id("auc-save");
        a.setItem(item);
        a.setSeller(new Bidder());
        a.getSeller().setUser_id("seller-save");
        a.setStartingPrice(new BigDecimal("1000"));
        a.setCurrentPrice(new BigDecimal("1000"));
        a.setMinIncrement(new BigDecimal("50"));
        a.setStartTime(LocalDateTime.now());
        a.setEndTime(LocalDateTime.now().plusDays(1));

        AuctionDAO.save(a);
        assertNotNull(AuctionDAO.findById("auc-save"));
        assertFalse(AuctionDAO.findAuctionHistory().isEmpty());
    }

    @Test
    void testUpdatesAndStatus() throws Exception {
        insertBaseData();
        insertAuction("auc-upd", "item-t1", "seller-t1", 100, false, false, "2024-01-01 00:00:00", "2099-01-01 00:00:00");

        assertTrue(AuctionDAO.approveAuction("auc-upd"));
        AuctionDAO.updateAuctionStatus("auc-upd", "CANCELLED");
        assertTrue(AuctionDAO.resumeAuction("auc-upd"));
        assertTrue(AuctionDAO.stopAuction("auc-upd"));
        assertTrue(AuctionDAO.updateEndTime("auc-upd", LocalDateTime.now().plusDays(5)));

        // Test ENDED branch in updateAuctionStatus
        assertDoesNotThrow(() -> AuctionDAO.updateAuctionStatus("auc-upd", "ENDED"));
    }

    @Test
    void testBidsHistoryAndJoined() throws Exception {
        insertBaseData();
        insertUser("b1", "bidder", "b@mail.com", "pass", "BIDDER", 100);
        insertAuction("auc-b", "item-t1", "seller-t1", 100, true, false, "2020-01-01 00:00:00", "2099-01-01 00:00:00");

        try(var ps = conn.prepareStatement("INSERT INTO bids VALUES ('bid1','auc-b','b1',150,NOW())")) {
            ps.executeUpdate();
        }

        assertFalse(AuctionDAO.findJoinedAuctions("b1").isEmpty());
        assertFalse(AuctionDAO.getBidHistory("auc-b").isEmpty());
        assertFalse(AuctionDAO.findAllAsStrings().isEmpty());
    }

    @Test
    void testNullAndErrors() {
        assertDoesNotThrow(() -> AuctionDAO.save(null));
        assertNull(AuctionDAO.findById("invalid"));
        assertFalse(AuctionDAO.stopAuction("invalid"));
        assertFalse(AuctionDAO.updateEndTime("invalid", LocalDateTime.now()));
    }
}
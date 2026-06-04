package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    private Auction auction;

    @BeforeEach
    void setUp() {
        auction = new Auction();
        auction.setAuction_id("a1");
        auction.setStartingPrice(new BigDecimal("100"));
        auction.setCurrentPrice(new BigDecimal("100"));
        auction.setMinIncrement(new BigDecimal("10"));
    }

    // ==================== getStatus ====================

    @Test
    void getStatus_cancelled_returnsCancelled() {
        auction.setCancelled(true);
        assertEquals(AuctionStatus.CANCELLED, auction.getStatus());
    }

    @Test
    void getStatus_notApproved_returnsPendingApproval() {
        auction.setApproved(false);
        assertEquals(AuctionStatus.PENDING_APPROVAL, auction.getStatus());
    }

    @Test
    void getStatus_beforeStart_returnsUpcoming() {
        auction.setApproved(true);
        auction.setStartTime(LocalDateTime.now().plusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(2));
        assertEquals(AuctionStatus.UPCOMING, auction.getStatus());
    }

    @Test
    void getStatus_afterEnd_returnsEnded() {
        auction.setApproved(true);
        auction.setStartTime(LocalDateTime.now().minusHours(2));
        auction.setEndTime(LocalDateTime.now().minusHours(1));
        assertEquals(AuctionStatus.ENDED, auction.getStatus());
    }

    @Test
    void getStatus_active_returnsActive() {
        auction.setApproved(true);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
    }

    // ==================== setStatus ====================

    @Test
    void setStatus_cancelled() {
        auction.setStatus(AuctionStatus.CANCELLED);
        assertTrue(auction.isCancelled());
    }

    @Test
    void setStatus_pendingApproval() {
        auction.setStatus(AuctionStatus.PENDING_APPROVAL);
        assertFalse(auction.isApproved());
    }

    @Test
    void setStatus_active() {
        auction.setStatus(AuctionStatus.ACTIVE);
        assertTrue(auction.isApproved());
        assertFalse(auction.isCancelled());
    }

    @Test
    void setStatus_ended() {
        auction.setStatus(AuctionStatus.ENDED);
        assertTrue(auction.isApproved());
    }

    // ==================== bids ====================

    @Test
    void addBid_null_ignored() {
        auction.addBid(null);
        assertEquals(0, auction.getBids().size());
    }

    @Test
    void addBid_valid_updatesCurrentPrice() {
        Bid bid = new Bid();
        bid.setAmount(new BigDecimal("200"));

        auction.addBid(bid);

        assertEquals(1, auction.getBids().size());
        assertEquals(new BigDecimal("200"), auction.getCurrentPrice());
    }

    @Test
    void addBid_multiple_lastWins() {
        Bid b1 = new Bid();
        b1.setAmount(new BigDecimal("200"));

        Bid b2 = new Bid();
        b2.setAmount(new BigDecimal("350"));

        auction.addBid(b1);
        auction.addBid(b2);

        assertEquals(new BigDecimal("350"), auction.getCurrentPrice());
        assertEquals(b2, auction.getHighestBid());
    }

    // ==================== highest bid ====================

    @Test
    void getHighestBid_empty_returnsNull() {
        assertNull(auction.getHighestBid());
    }

    @Test
    void getHighestBid_single_returnsBid() {
        Bid bid = new Bid();
        bid.setAmount(new BigDecimal("500"));

        auction.addBid(bid);

        assertEquals(bid, auction.getHighestBid());
    }

    // ==================== bids list safety ====================

    @Test
    void setBids_replacesList() {
        ArrayList<Bid> list = new ArrayList<>();

        Bid b = new Bid();
        b.setAmount(new BigDecimal("123"));
        list.add(b);

        auction.setBids(list);

        assertEquals(1, auction.getBids().size());
    }

    @Test
    void setBids_null_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> auction.setBids(null));
    }
}
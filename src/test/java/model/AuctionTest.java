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

    // ==================== getStatus() ====================

    @Test
    void getStatus_cancelled_returnsCancelled() {
        auction.setCancelled(true);
        auction.setApproved(true);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        assertEquals(AuctionStatus.CANCELLED, auction.getStatus());
    }

    @Test
    void getStatus_notApproved_returnsPendingApproval() {
        auction.setCancelled(false);
        auction.setApproved(false);
        assertEquals(AuctionStatus.PENDING_APPROVAL, auction.getStatus());
    }

    @Test
    void getStatus_approvedBeforeStart_returnsUpcoming() {
        auction.setCancelled(false);
        auction.setApproved(true);
        auction.setStartTime(LocalDateTime.now().plusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(2));
        assertEquals(AuctionStatus.UPCOMING, auction.getStatus());
    }

    @Test
    void getStatus_approvedAfterEnd_returnsEnded() {
        auction.setCancelled(false);
        auction.setApproved(true);
        auction.setStartTime(LocalDateTime.now().minusHours(2));
        auction.setEndTime(LocalDateTime.now().minusHours(1));
        assertEquals(AuctionStatus.ENDED, auction.getStatus());
    }

    @Test
    void getStatus_approvedDuringWindow_returnsActive() {
        auction.setCancelled(false);
        auction.setApproved(true);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
    }

    @Test
    void getStatus_nullStartTime_treatedAsActive() {
        auction.setCancelled(false);
        auction.setApproved(true);
        auction.setStartTime(null);
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
    }

    @Test
    void getStatus_nullEndTime_treatedAsActive() {
        auction.setCancelled(false);
        auction.setApproved(true);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(null);
        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
    }

    // ==================== setStatus() ====================

    @Test
    void setStatus_cancelled_setsCancelledTrue() {
        auction.setStatus(AuctionStatus.CANCELLED);
        assertTrue(auction.isCancelled());
    }

    @Test
    void setStatus_pendingApproval_setsApprovedFalse() {
        auction.setApproved(true);
        auction.setStatus(AuctionStatus.PENDING_APPROVAL);
        assertFalse(auction.isApproved());
        assertFalse(auction.isCancelled());
    }

    @Test
    void setStatus_active_setsApprovedTrueAndNotCancelled() {
        auction.setStatus(AuctionStatus.ACTIVE);
        assertTrue(auction.isApproved());
        assertFalse(auction.isCancelled());
    }

    @Test
    void setStatus_ended_setsApprovedTrue() {
        auction.setStatus(AuctionStatus.ENDED);
        assertTrue(auction.isApproved());
        assertFalse(auction.isCancelled());
    }

    // ==================== addBid() ====================

    @Test
    void addBid_null_doesNothing() {
        auction.addBid(null);
        assertTrue(auction.getBids().isEmpty());
        assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
    }

    @Test
    void addBid_validBid_updatesCurrentPrice() {
        Bid bid = new Bid();
        bid.setAmount(new BigDecimal("200"));
        auction.addBid(bid);
        assertEquals(new BigDecimal("200"), auction.getCurrentPrice());
        assertEquals(1, auction.getBids().size());
    }

    @Test
    void addBid_multipleBids_lastBidBecomesCurrentPrice() {
        Bid b1 = new Bid(); b1.setAmount(new BigDecimal("200"));
        Bid b2 = new Bid(); b2.setAmount(new BigDecimal("350"));
        auction.addBid(b1);
        auction.addBid(b2);
        assertEquals(new BigDecimal("350"), auction.getCurrentPrice());
        assertEquals(2, auction.getBids().size());
    }

    // ==================== getHighestBid() ====================

    @Test
    void getHighestBid_noBids_returnsNull() {
        assertNull(auction.getHighestBid());
    }

    @Test
    void getHighestBid_oneBid_returnsThatBid() {
        Bid bid = new Bid(); bid.setAmount(new BigDecimal("500"));
        auction.addBid(bid);
        assertEquals(bid, auction.getHighestBid());
    }

    @Test
    void getHighestBid_multipleBids_returnsLast() {
        Bid b1 = new Bid(); b1.setAmount(new BigDecimal("200"));
        Bid b2 = new Bid(); b2.setAmount(new BigDecimal("400"));
        auction.addBid(b1);
        auction.addBid(b2);
        assertEquals(b2, auction.getHighestBid());
    }

    // ==================== setBids() ====================

    @Test
    void setBids_null_replacesWithNull() {
        auction.setBids(null);
        assertNull(auction.getBids());
    }

    @Test
    void setBids_emptyList_getHighestBidReturnsNull() {
        auction.setBids(new ArrayList<>());
        assertNull(auction.getHighestBid());
    }
}
package server.dao;

import model.Bid;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BidDAOTest extends TiDBRollbackBase {

    // Tạo hậu tố ngẫu nhiên cho mỗi lần chạy test để không trùng username/id
    private final String suffix = UUID.randomUUID().toString().substring(0, 8);

    // Các ID động để dùng xuyên suốt trong class
    private final String sellerId = "seller-" + suffix;
    private final String bidder1 = "bidder1-" + suffix;
    private final String bidder2 = "bidder2-" + suffix;
    private final String itemId = "item-" + suffix;
    private final String auctionId = "auction-" + suffix;

    private void setup() throws Exception {
        // Dùng username và id động để tránh lỗi Duplicate entry '?' for key 'users.username'
        insertUser(sellerId, "tseller" + suffix, "tseller" + suffix + "@mail.com", "pass", "SELLER", 0);
        insertUser(bidder1, "talice" + suffix, "talice" + suffix + "@mail.com", "pass", "BIDDER", 10000);
        insertUser(bidder2, "tbob" + suffix, "tbob" + suffix + "@mail.com", "pass", "BIDDER", 10000);

        insertItem(itemId, "TestWatch", "ELECTRONICS");
        insertAuction(auctionId, itemId, sellerId, 500, true, false,
                "2024-01-01 10:00:00", "2099-12-31 23:59:59");
    }

    // ==================== placeBid ====================

    @Test
    void placeBid_validBid_returnsTrueAndPersisted() throws Exception {
        setup();
        assertTrue(BidDAO.placeBid(auctionId, bidder1, new BigDecimal("600")));

        try (var ps = conn.prepareStatement("SELECT bid_amount FROM bids WHERE auction_id = ?")) {
            ps.setString(1, auctionId);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals(0, new BigDecimal("600").compareTo(rs.getBigDecimal("bid_amount")));
        }
    }

    @Test
    void placeBid_updatesAuctionCurrentPrice() throws Exception {
        setup();
        BidDAO.placeBid(auctionId, bidder1, new BigDecimal("750"));

        try (var ps = conn.prepareStatement("SELECT current_price FROM auctions WHERE auction_id = ?")) {
            ps.setString(1, auctionId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(0, new BigDecimal("750").compareTo(rs.getBigDecimal("current_price")));
        }
    }

    @Test
    void placeBid_multipleBids_allPersisted() throws Exception {
        setup();
        BidDAO.placeBid(auctionId, bidder1, new BigDecimal("600"));
        BidDAO.placeBid(auctionId, bidder2, new BigDecimal("700"));
        BidDAO.placeBid(auctionId, bidder1, new BigDecimal("800"));

        try (var ps = conn.prepareStatement("SELECT COUNT(*) FROM bids WHERE auction_id = ?")) {
            ps.setString(1, auctionId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(3, rs.getInt(1));
        }
    }

    @Test
    void placeBid_lastBidBecomesCurrentPrice() throws Exception {
        setup();
        BidDAO.placeBid(auctionId, bidder1, new BigDecimal("600"));
        BidDAO.placeBid(auctionId, bidder2, new BigDecimal("900"));

        try (var ps = conn.prepareStatement("SELECT current_price FROM auctions WHERE auction_id = ?")) {
            ps.setString(1, auctionId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(0, new BigDecimal("900").compareTo(rs.getBigDecimal("current_price")));
        }
    }

    // ==================== getBidsByAuctionId ====================

    @Test
    void getBidsByAuctionId_noBids_returnsEmpty() throws Exception {
        setup();
        assertTrue(BidDAO.getBidsByAuctionId(auctionId).isEmpty());
    }

    @Test
    void getBidsByAuctionId_withBids_returnsBidsWithUser() throws Exception {
        setup();
        BidDAO.placeBid(auctionId, bidder1, new BigDecimal("600"));
        BidDAO.placeBid(auctionId, bidder2, new BigDecimal("700"));

        List<Bid> bids = BidDAO.getBidsByAuctionId(auctionId);
        assertEquals(2, bids.size());
        // Kiểm tra chứa username có hậu tố ngẫu nhiên
        assertTrue(bids.stream().anyMatch(b -> ("talice" + suffix).equals(b.getBidder().getUsername())));
        assertTrue(bids.stream().anyMatch(b -> ("tbob" + suffix).equals(b.getBidder().getUsername())));
    }

    @Test
    void getBidsByAuctionId_unknownAuction_returnsEmpty() {
        assertTrue(BidDAO.getBidsByAuctionId("no-such-auction-" + suffix).isEmpty());
    }

    @Test
    void getBidsByAuctionId_sortedAscending() throws Exception {
        setup();
        BidDAO.placeBid(auctionId, bidder1, new BigDecimal("600"));
        BidDAO.placeBid(auctionId, bidder2, new BigDecimal("700"));

        List<Bid> bids = BidDAO.getBidsByAuctionId(auctionId);
        assertTrue(bids.get(0).getAmount().compareTo(bids.get(1).getAmount()) <= 0);
    }

    // ==================== getHighestBidAmount ====================

    @Test
    void getHighestBidAmount_noBids_returnsZero() throws Exception {
        setup();
        assertEquals(0, BigDecimal.ZERO.compareTo(BidDAO.getHighestBidAmount(auctionId)));
    }

    @Test
    void getHighestBidAmount_multipleBids_returnsMax() throws Exception {
        setup();
        BidDAO.placeBid(auctionId, bidder1, new BigDecimal("600"));
        BidDAO.placeBid(auctionId, bidder2, new BigDecimal("900"));
        BidDAO.placeBid(auctionId, bidder1, new BigDecimal("750"));

        assertEquals(0, new BigDecimal("900").compareTo(BidDAO.getHighestBidAmount(auctionId)));
    }

    @Test
    void getHighestBidAmount_unknownAuction_returnsZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(BidDAO.getHighestBidAmount("no-such-" + suffix)));
    }
}
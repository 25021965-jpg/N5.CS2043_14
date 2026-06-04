package server.service;

import model.*;
import model.Entity.User.Bidder;
import model.Entity.User.Role;
import model.Entity.User.Seller;
import model.Entity.User.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.dao.UserDAO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BidServiceTest {

    private User bidder;
    private Auction auction;

    @BeforeEach
    void setUp() {

        bidder = new Bidder();
        bidder.setUser_id("user-1");
        bidder.setUsername("alice");
        bidder.setRole(Role.BIDDER);
        bidder.setBalance(new BigDecimal("10000"));

        auction = new Auction();
        auction.setAuction_id("auction-1");
        auction.setCancelled(false);
        auction.setApproved(true);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        auction.setCurrentPrice(new BigDecimal("500"));
        auction.setMinIncrement(new BigDecimal("50"));

        User seller = new Seller();
        seller.setUser_id("seller-1");
        seller.setUsername("seller");

        auction.setSeller(seller);
    }

    // ================= ROLE =================

    @Test
    void placeBid_sellerRole_returnedPermissionError() {

        bidder.setRole(Role.SELLER);

        String result =
                BidService.placeBid(
                        bidder,
                        auction,
                        new BigDecimal("600"));

        assertTrue(result.startsWith("ERROR|"));
        assertTrue(result.contains("permission"));
    }

    @Test
    void placeBid_adminRole_passesRoleCheck() {

        bidder.setRole(Role.ADMIN);

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class);
             MockedStatic<BidDAO> bidDao = Mockito.mockStatic(BidDAO.class)) {

            userDao.when(() -> UserDAO.getVirtualBalance("user-1"))
                    .thenReturn(new BigDecimal("1000"));

            userDao.when(() ->
                            UserDAO.deductVirtualBalance(
                                    "user-1",
                                    new BigDecimal("600")))
                    .thenReturn(true);

            bidDao.when(() ->
                            BidDAO.placeBid(
                                    "auction-1",
                                    "user-1",
                                    new BigDecimal("600")))
                    .thenReturn(true);

            String result =
                    BidService.placeBid(
                            bidder,
                            auction,
                            new BigDecimal("600"));

            assertTrue(result.startsWith("BID_SUCCESS"));
        }
    }

    // ================= STATUS =================

    @Test
    void placeBid_cancelledAuction_returnedError() {

        auction.setCancelled(true);

        String result =
                BidService.placeBid(
                        bidder,
                        auction,
                        new BigDecimal("600"));

        assertTrue(result.contains("not active"));
    }

    @Test
    void placeBid_pendingAuction_returnedError() {

        auction.setApproved(false);

        String result =
                BidService.placeBid(
                        bidder,
                        auction,
                        new BigDecimal("600"));

        assertTrue(result.contains("not active"));
    }

    @Test
    void placeBid_upcomingAuction_returnedError() {

        auction.setStartTime(LocalDateTime.now().plusHours(1));

        String result =
                BidService.placeBid(
                        bidder,
                        auction,
                        new BigDecimal("600"));

        assertTrue(result.contains("not active"));
    }

    @Test
    void placeBid_endedAuction_returnedError() {

        auction.setEndTime(LocalDateTime.now().minusHours(1));

        String result =
                BidService.placeBid(
                        bidder,
                        auction,
                        new BigDecimal("600"));

        assertTrue(result.contains("not active"));
    }

    // ================= BID AMOUNT =================

    @Test
    void placeBid_belowMinimum_returnedError() {

        String result =
                BidService.placeBid(
                        bidder,
                        auction,
                        new BigDecimal("549"));

        assertTrue(result.contains("Bid amount must be at least"));
    }

    @Test
    void placeBid_belowCurrentPrice_returnedError() {

        String result =
                BidService.placeBid(
                        bidder,
                        auction,
                        new BigDecimal("400"));

        assertTrue(result.contains("Bid amount must be at least"));
    }

    // ================= BALANCE =================

    @Test
    void placeBid_insufficientBalance_returnedError() {

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {

            userDao.when(() -> UserDAO.getVirtualBalance("user-1"))
                    .thenReturn(new BigDecimal("100"));

            String result =
                    BidService.placeBid(
                            bidder,
                            auction,
                            new BigDecimal("600"));

            assertTrue(result.contains("Insufficient balance"));
        }
    }

    @Test
    void placeBid_zeroBalance_returnedError() {

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {

            userDao.when(() -> UserDAO.getVirtualBalance("user-1"))
                    .thenReturn(BigDecimal.ZERO);

            String result =
                    BidService.placeBid(
                            bidder,
                            auction,
                            new BigDecimal("600"));

            assertTrue(result.contains("Insufficient balance"));
        }
    }

    @Test
    void placeBid_exactBalance_passesBalanceCheck() {

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class);
             MockedStatic<BidDAO> bidDao = Mockito.mockStatic(BidDAO.class);
             MockedStatic<AuctionDAO> ignored = Mockito.mockStatic(AuctionDAO.class)) {

            userDao.when(() -> UserDAO.getVirtualBalance("user-1"))
                    .thenReturn(new BigDecimal("600"));

            userDao.when(() ->
                            UserDAO.deductVirtualBalance(
                                    "user-1",
                                    new BigDecimal("600")))
                    .thenReturn(true);

            bidDao.when(() ->
                            BidDAO.placeBid(
                                    "auction-1",
                                    "user-1",
                                    new BigDecimal("600")))
                    .thenReturn(true);

            String result =
                    BidService.placeBid(
                            bidder,
                            auction,
                            new BigDecimal("600"));

            assertTrue(result.startsWith("BID_SUCCESS"));
        }
    }

    @Test
    void placeBid_deductFail_returnsError() {

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {

            userDao.when(() -> UserDAO.getVirtualBalance("user-1"))
                    .thenReturn(new BigDecimal("1000"));

            userDao.when(() ->
                            UserDAO.deductVirtualBalance(
                                    "user-1",
                                    new BigDecimal("600")))
                    .thenReturn(false);

            String result =
                    BidService.placeBid(
                            bidder,
                            auction,
                            new BigDecimal("600"));

            assertTrue(result.contains("Failed to deduct"));
        }
    }

    @Test
    void placeBid_bidDaoFail_returnsError() {

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class);
             MockedStatic<BidDAO> bidDao = Mockito.mockStatic(BidDAO.class)) {

            userDao.when(() -> UserDAO.getVirtualBalance("user-1"))
                    .thenReturn(new BigDecimal("1000"));

            userDao.when(() ->
                            UserDAO.deductVirtualBalance(
                                    "user-1",
                                    new BigDecimal("600")))
                    .thenReturn(true);

            bidDao.when(() ->
                            BidDAO.placeBid(
                                    "auction-1",
                                    "user-1",
                                    new BigDecimal("600")))
                    .thenReturn(false);

            String result =
                    BidService.placeBid(
                            bidder,
                            auction,
                            new BigDecimal("600"));

            assertEquals(
                    "ERROR|Database error during bidding",
                    result
            );
        }
    }

    // ================= ANTI SNIPE =================

    @Test
    void checkAntiSnipe_nullEndTime_returnsNull() {

        auction.setEndTime(null);

        assertNull(
                BidService.checkAntiSnipe(auction)
        );
    }

    @Test
    void checkAntiSnipe_maxExtend_returnsNull() {

        auction.setExtendCount(
                BidService.ANTI_SNIPE_MAX_EXTENDS
        );

        assertNull(
                BidService.checkAntiSnipe(auction)
        );
    }

    @Test
    void checkAntiSnipe_insideWindow_returnsNewTime() {

        auction.setEndTime(
                LocalDateTime.now().plusSeconds(5)
        );

        LocalDateTime result =
                BidService.checkAntiSnipe(auction);

        assertNotNull(result);
        assertEquals(1, auction.getExtendCount());
    }

    @Test
    void checkAntiSnipe_outsideWindow_returnsNull() {

        auction.setEndTime(
                LocalDateTime.now().plusMinutes(5)
        );

        assertNull(
                BidService.checkAntiSnipe(auction)
        );
    }

    // ================= SETTLE AUCTION =================

    @Test
    void settleAuction_noHighestBid_returns() {

        try (MockedStatic<BidDAO> bidDao =
                     Mockito.mockStatic(BidDAO.class)) {

            bidDao.when(() ->
                            BidDAO.getHighestBidAmount("auction-1"))
                    .thenReturn(null);

            assertDoesNotThrow(() ->
                    BidService.settleAuction(auction));
        }
    }

    @Test
    void settleAuction_emptyBidList_returns() {

        try (MockedStatic<BidDAO> bidDao =
                     Mockito.mockStatic(BidDAO.class)) {

            bidDao.when(() ->
                            BidDAO.getHighestBidAmount("auction-1"))
                    .thenReturn(new BigDecimal("1000"));

            bidDao.when(() ->
                            BidDAO.getBidsByAuctionId("auction-1"))
                    .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() ->
                    BidService.settleAuction(auction));
        }
    }

    @Test
    void settleAuction_winnerNull_returns() {

        Bid bid = new Bid();
        bid.setBidder(null);

        try (MockedStatic<BidDAO> bidDao =
                     Mockito.mockStatic(BidDAO.class)) {

            bidDao.when(() ->
                            BidDAO.getHighestBidAmount("auction-1"))
                    .thenReturn(new BigDecimal("1000"));

            bidDao.when(() ->
                            BidDAO.getBidsByAuctionId("auction-1"))
                    .thenReturn(List.of(bid));

            assertDoesNotThrow(() ->
                    BidService.settleAuction(auction));
        }
    }

    @Test
    void settleAuction_sellerNull_returns() {

        auction.setSeller(null);

        User winner = new Seller();
        winner.setUser_id("winner");

        Bid bid = new Bid();
        bid.setBidder(winner);

        try (MockedStatic<BidDAO> bidDao =
                     Mockito.mockStatic(BidDAO.class)) {

            bidDao.when(() ->
                            BidDAO.getHighestBidAmount("auction-1"))
                    .thenReturn(new BigDecimal("1000"));

            bidDao.when(() ->
                            BidDAO.getBidsByAuctionId("auction-1"))
                    .thenReturn(List.of(bid));

            assertDoesNotThrow(() ->
                    BidService.settleAuction(auction));
        }
    }

    @Test
    void settleAuction_userNotFound_returns() {

        User winner = new Bidder();
        winner.setUser_id("winner");

        Bid bid = new Bid();
        bid.setBidder(winner);

        try (MockedStatic<BidDAO> bidDao =
                     Mockito.mockStatic(BidDAO.class);
             MockedStatic<UserDAO> userDao =
                     Mockito.mockStatic(UserDAO.class)) {

            bidDao.when(() ->
                            BidDAO.getHighestBidAmount("auction-1"))
                    .thenReturn(new BigDecimal("1000"));

            bidDao.when(() ->
                            BidDAO.getBidsByAuctionId("auction-1"))
                    .thenReturn(List.of(bid));

            userDao.when(() ->
                            UserDAO.getUserById("winner"))
                    .thenReturn(null);

            assertDoesNotThrow(() ->
                    BidService.settleAuction(auction));
        }
    }

    @Test
    void settleAuction_insufficientBalance_returns() {

        User winner = new Bidder();
        winner.setUser_id("winner");

        Bid bid = new Bid();
        bid.setBidder(winner);

        User winnerDb = new Bidder();
        winnerDb.setBalance(new BigDecimal("100"));

        User sellerDb = new Seller();
        sellerDb.setBalance(new BigDecimal("1000"));

        try (MockedStatic<BidDAO> bidDao =
                     Mockito.mockStatic(BidDAO.class);
             MockedStatic<UserDAO> userDao =
                     Mockito.mockStatic(UserDAO.class)) {

            bidDao.when(() ->
                            BidDAO.getHighestBidAmount("auction-1"))
                    .thenReturn(new BigDecimal("1000"));

            bidDao.when(() ->
                            BidDAO.getBidsByAuctionId("auction-1"))
                    .thenReturn(List.of(bid));

            userDao.when(() ->
                            UserDAO.getUserById("winner"))
                    .thenReturn(winnerDb);

            userDao.when(() ->
                            UserDAO.getUserById("seller-1"))
                    .thenReturn(sellerDb);

            assertDoesNotThrow(() ->
                    BidService.settleAuction(auction));
        }
    }


}
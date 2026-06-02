package server.service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.dao.UserDAO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BidServiceTest {

    private User bidder;
    private Auction auction;

    @BeforeEach
    void setUp() {
        bidder = new User();
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

        User seller = new User();
        seller.setUser_id("seller-1");
        auction.setSeller(seller);
    }

    // ==================== Role check ====================

    @Test
    void placeBid_sellerRole_returnedPermissionError() {
        bidder.setRole(Role.SELLER);

        String result = BidService.placeBid(
                bidder,
                auction,
                new BigDecimal("600")
        );

        assertTrue(result.startsWith("ERROR|"));
        assertTrue(result.contains("permission"));
    }

    @Test
    void placeBid_adminRole_passesRoleCheck() {
        bidder.setRole(Role.ADMIN);

        String result = BidService.placeBid(
                bidder,
                auction,
                new BigDecimal("600")
        );

        assertFalse(result.contains("permission"));
    }

    // ==================== Auction status ====================

    @Test
    void placeBid_cancelledAuction_returnedError() {
        auction.setCancelled(true);

        String result = BidService.placeBid(
                bidder,
                auction,
                new BigDecimal("600")
        );

        assertTrue(result.startsWith("ERROR|"));
        assertTrue(result.contains("not active"));
    }

    @Test
    void placeBid_pendingAuction_returnedError() {
        auction.setApproved(false);

        String result = BidService.placeBid(
                bidder,
                auction,
                new BigDecimal("600")
        );

        assertTrue(result.startsWith("ERROR|"));
        assertTrue(result.contains("not active"));
    }

    @Test
    void placeBid_upcomingAuction_returnedError() {
        auction.setStartTime(LocalDateTime.now().plusHours(1));

        String result = BidService.placeBid(
                bidder,
                auction,
                new BigDecimal("600")
        );

        assertTrue(result.startsWith("ERROR|"));
        assertTrue(result.contains("not active"));
    }

    @Test
    void placeBid_endedAuction_returnedError() {
        auction.setEndTime(LocalDateTime.now().minusHours(1));

        String result = BidService.placeBid(
                bidder,
                auction,
                new BigDecimal("600")
        );

        assertTrue(result.startsWith("ERROR|"));
        assertTrue(result.contains("not active"));
    }

    // ==================== Minimum bid check ====================

    @Test
    void placeBid_belowMinimum_returnedError() {
        String result = BidService.placeBid(
                bidder,
                auction,
                new BigDecimal("549")
        );

        assertTrue(result.startsWith("ERROR|"));
        assertTrue(result.contains("Bid amount must be at least"));
    }

    @Test
    void placeBid_belowCurrentPrice_returnedError() {
        String result = BidService.placeBid(
                bidder,
                auction,
                new BigDecimal("400")
        );

        assertTrue(result.startsWith("ERROR|"));
        assertTrue(result.contains("Bid amount must be at least"));
    }

    // ==================== Balance check ====================

    @Test
    void placeBid_insufficientBalance_returnedError() {

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {

            userDao.when(() -> UserDAO.getVirtualBalance("user-1"))
                    .thenReturn(new BigDecimal("100"));

            String result = BidService.placeBid(
                    bidder,
                    auction,
                    new BigDecimal("600")
            );

            assertTrue(result.startsWith("ERROR|"));
            assertTrue(result.contains("Insufficient balance"));
        }
    }

    @Test
    void placeBid_zeroBalance_returnedError() {

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {

            userDao.when(() -> UserDAO.getVirtualBalance("user-1"))
                    .thenReturn(BigDecimal.ZERO);

            String result = BidService.placeBid(
                    bidder,
                    auction,
                    new BigDecimal("600")
            );

            assertTrue(result.startsWith("ERROR|"));
            assertTrue(result.contains("Insufficient balance"));
        }
    }

    @Test
    void placeBid_exactBalance_passesBalanceCheck() {

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class);
             MockedStatic<BidDAO> bidDao = Mockito.mockStatic(BidDAO.class);
             MockedStatic<AuctionDAO> auctionDao = Mockito.mockStatic(AuctionDAO.class)) {

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

            String result = BidService.placeBid(
                    bidder,
                    auction,
                    new BigDecimal("600")
            );

            assertFalse(result.contains("Insufficient balance"));
            assertTrue(result.startsWith("BID_SUCCESS"));
        }
    }
}
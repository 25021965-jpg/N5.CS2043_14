package test;
import model.*;
import service.*;
import exception.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BidServiceTest {

    @Test
    void testValidBid() {
        User user = new User();
        user.setRoles(List.of(Role.BIDDER));

        Auction auction = new Auction();
        auction.setCurrentPrice(BigDecimal.valueOf(100));
        auction.setMinIncrement(BigDecimal.valueOf(10));
        auction.setStatus(AuctionStatus.ACTIVE);

        BidService service = new BidService();

        Bid bid = service.placeBid(user, auction, BigDecimal.valueOf(120));

        assertEquals(120, bid.getAmount().intValue());
    }

    @Test
    void testBidTooLow() {
        User user = new User();
        user.setRoles(List.of(Role.BIDDER));

        Auction auction = new Auction();
        auction.setCurrentPrice(BigDecimal.valueOf(100));
        auction.setMinIncrement(BigDecimal.valueOf(10));
        auction.setStatus(AuctionStatus.ACTIVE);

        BidService service = new BidService();

        assertThrows(InvalidBidException.class, () -> {
            service.placeBid(user, auction, BigDecimal.valueOf(105));
        });
    }

    @Test
    void testAuctionClosed() {
        User user = new User();
        user.setRoles(List.of(Role.BIDDER));

        Auction auction = new Auction();
        auction.setStatus(AuctionStatus.ENDED);

        BidService service = new BidService();

        assertThrows(AuctionClosedException.class, () -> {
            service.placeBid(user, auction, BigDecimal.valueOf(200));
        });
    }
}
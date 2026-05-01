import model.*;
import service.*;
import exception.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BidServiceTest {

    private User createBidder() {
        return new User(
                "1",
                "user",
                "user@gmail.com",
                "123",
                List.of(Role.BIDDER)
        );
    }

    private Auction createActiveAuction() {
        Auction auction = new Auction();
        auction.setCurrentPrice(BigDecimal.valueOf(100));
        auction.setMinIncrement(BigDecimal.valueOf(10));
        auction.setStatus(AuctionStatus.ACTIVE);
        return auction;
    }

    @Test
    void testValidBid() {
        User user = createBidder();
        Auction auction = createActiveAuction();

        BidService service = new BidService();

        Bid bid = service.placeBid(user, auction, BigDecimal.valueOf(120));

        assertEquals(120, bid.getAmount().intValue());
    }

    @Test
    void testBidTooLow() {
        User user = createBidder();
        Auction auction = createActiveAuction();

        BidService service = new BidService();

        assertThrows(InvalidBidException.class, () -> {
            service.placeBid(user, auction, BigDecimal.valueOf(105));
        });
    }

    @Test
    void testAuctionClosed() {
        User user = createBidder();

        Auction auction = new Auction();
        auction.setStatus(AuctionStatus.ENDED);

        BidService service = new BidService();

        assertThrows(AuctionClosedException.class, () -> {
            service.placeBid(user, auction, BigDecimal.valueOf(200));
        });
    }
}
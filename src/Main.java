import model.*;
import service.*;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        User user = new User();
        user.setRoles(List.of(Role.BIDDER));

        Item item = new Item();

        Auction auction = new Auction();
        auction.setCurrentPrice(BigDecimal.valueOf(100));
        auction.setMinIncrement(BigDecimal.valueOf(10));
        auction.setStatus(AuctionStatus.ACTIVE);

        BidService bidService = new BidService();

        Bid bid = bidService.placeBid(
                user,
                auction,
                BigDecimal.valueOf(120)
        );

        System.out.println("Bid placed: " + bid.getAmount());
    }
}
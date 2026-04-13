import model.*;
import service.*;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        User user = new User();
        user.setRoles(List.of(Role.BIDDER));

        Auction auction = new Auction();
        auction.setCurrentPrice(BigDecimal.valueOf(100));
        auction.setMinIncrement(BigDecimal.valueOf(10));
        auction.setStatus(AuctionStatus.ACTIVE);

        BidService bidService = new BidService();

        try {
            Bid bid = bidService.placeBid(
                    user,
                    auction,
                    BigDecimal.valueOf(105)
            );
            System.out.println("Bid placed: " + bid.getAmount());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
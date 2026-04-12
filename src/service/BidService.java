package service;

import model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidService {

    public Bid placeBid(User user, Auction auction, BigDecimal amount) {

        if (!user.hasRole(Role.BIDDER)) {
            throw new RuntimeException("User is not bidder");
        }

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new RuntimeException("Auction not active");
        }

        BigDecimal minPrice = auction.getCurrentPrice()
                .add(auction.getMinIncrement());

        if (amount.compareTo(minPrice) < 0) {
            throw new RuntimeException("Bid too low");
        }

        Bid bid = new Bid();
        bid.setAmount(amount);
        bid.setBidder(user);
        bid.setAuction(auction);
        bid.setTimestamp(LocalDateTime.now());

        auction.setCurrentPrice(amount);

        return bid;
    }
}
package service;

import model.*;

import java.math.BigDecimal;

public class BidService{

    public synchronized Bid placeBid(User user, Auction auction, BigDecimal amount) {

        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        if (!user.hasRole(Role.BIDDER)) {
            throw new RuntimeException("Not allowed to bid");
        }

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new RuntimeException("Auction is closed");
        }

        BigDecimal minPrice = auction.getCurrentPrice().add(auction.getMinIncrement());

        if (amount.compareTo(minPrice) < 0) {
            throw new RuntimeException("Bid must be >= " + minPrice);
        }

        Bid bid = new Bid(user, amount);

        auction.addBid(bid);

        return bid;
    }
}
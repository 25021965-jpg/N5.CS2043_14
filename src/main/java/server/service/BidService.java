package server.service;

import model.*;
import server.exception.*;

import java.math.BigDecimal;

public class BidService{

    public synchronized Bid placeBid(User user, Auction auction, BigDecimal amount) {

        if (user == null) {
            throw new AuthenticationException("User not logged in");
        }

        if (!user.hasRole(Role.BIDDER)) {
            throw new InvalidBidException("Not allowed to bid");
        }

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new AuctionClosedException("Auction is closed");
        }

        BigDecimal minPrice = auction.getCurrentPrice().add(auction.getMinIncrement());

        if (amount.compareTo(minPrice) < 0) {
            throw new InvalidBidException("Bid must be >= " + minPrice);
        }

        Bid bid = new Bid(user, amount);

        auction.addBid(bid);

        return bid;
    }
}
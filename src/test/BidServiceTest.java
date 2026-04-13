package service;

import model.*;
import exception.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidService {

    public Bid placeBid(User user, Auction auction, BigDecimal amount) {

        if (!user.hasRole(Role.BIDDER)) {
            throw new AuthenticationException("User is not bidder");
        }

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new AuctionClosedException("Auction is not active");
        }

        BigDecimal minPrice = auction.getCurrentPrice()
                .add(auction.getMinIncrement());

        if (amount.compareTo(minPrice) < 0) {
            throw new InvalidBidException("Bid too low");
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
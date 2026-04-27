package service;

import exception.AuthenticationException;
import exception.AuctionClosedException;
import exception.InvalidBidException;
import model.Auction;
import model.AuctionStatus;
import model.Bid;
import model.Role;
import model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidService {

    public synchronized Bid placeBid(User user, Auction auction, BigDecimal amount) {
        if (user == null) {
            throw new AuthenticationException("Please login first");
        }

        if (!user.hasRole(Role.BIDDER)) {
            throw new AuthenticationException("User is not bidder");
        }

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new AuctionClosedException("Auction is not active");
        }

        BigDecimal minPrice = auction.getCurrentPrice().add(auction.getMinIncrement());
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
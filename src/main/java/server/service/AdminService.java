package server.service;

import model.*;

public class AdminService {

    public void banUser(User user) {
        user.setVerified(false);
    }

    public void verifyUser(User user) {
        user.setVerified(true);
    }

    public void approveAuction(Auction auction) {
        auction.setStatus(AuctionStatus.ACTIVE);
    }

    public void removeAuction(Auction auction) {
        auction.setStatus(AuctionStatus.ENDED);
    }
}
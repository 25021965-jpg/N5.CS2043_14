package client.service;

import model.Auction;
import model.AuctionStatus;
import model.Bid;
import model.Entity.User.User;
import model.ParticipationStatus;

public class AuctionParticipationService {

    public static ParticipationStatus resolveUserStatus(Auction auction, User user) {
        if (auction == null || user == null) return ParticipationStatus.NOT_JOINED;

        Bid highest = auction.getHighestBid();

        // chưa có bid nào
        if (highest == null) {
            return ParticipationStatus.NOT_JOINED;
        }

        String topUsername = highest.getUsername();

        if (topUsername != null && topUsername.equals(user.getUsername())) {
            return ParticipationStatus.LEADING;
        }

        boolean userHasBid = auction.getBids()
                .stream()
                .anyMatch(b -> user.getUsername().equals(b.getUsername()));

        if (!userHasBid) {
            return ParticipationStatus.NOT_JOINED;
        }

        return ParticipationStatus.OUTBID;
    }

    public static ParticipationStatus resolveFinalStatus(Auction auction, User user) {
        if (auction == null || user == null) return null;

        if (auction.getStatus() != AuctionStatus.ENDED) {
            return resolveUserStatus(auction, user);
        }

        Bid highest = auction.getHighestBid();
        if (highest == null) return ParticipationStatus.LOST;

        if (user.getUsername().equals(highest.getUsername())) {
            return ParticipationStatus.WON;
        }

        return ParticipationStatus.LOST;
    }
}
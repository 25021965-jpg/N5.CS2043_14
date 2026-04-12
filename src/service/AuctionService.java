package service;

import model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionService {

    public Auction createAuction(User seller, Item item, BigDecimal startPrice) {

        if (!seller.hasRole(Role.SELLER)) {
            throw new RuntimeException("Not seller");
        }

        Auction auction = new Auction();
        auction.setSeller(seller);
        auction.setItem(item);
        auction.setCurrentPrice(startPrice);
        auction.setMinIncrement(BigDecimal.valueOf(10));
        auction.setStatus(AuctionStatus.ACTIVE);

        return auction;
    }

    public void uploadProductInfo(Item item, List<String> images) {
        item.setImages(images);
    }

    public void closeAuction(Auction auction) {
        auction.setStatus(AuctionStatus.ENDED);
    }
}
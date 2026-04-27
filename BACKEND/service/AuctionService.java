package service;

import model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AuctionService {

    private List<Auction> auctions = new ArrayList<>();

    public AuctionService() {

        List<Auction> loaded = FileService.load("auctions.dat");

        if (loaded != null) {
            auctions.addAll(loaded);
        }
    }

    public Auction createAuction(User seller, Item item, BigDecimal startPrice) {

        if (seller == null || !seller.hasRole(Role.SELLER)) {
            throw new RuntimeException("User is not seller");
        }

        Auction auction = new Auction();
        auction.setSeller(seller);
        auction.setItem(item);
        auction.setCurrentPrice(startPrice);
        auction.setMinIncrement(BigDecimal.valueOf(10));
        auction.setStatus(AuctionStatus.ACTIVE);

        auctions.add(auction);

        FileService.save("auctions.dat", auctions);

        return auction;
    }

    public void uploadProductInfo(Item item, List<String> images) {
        item.setImages(images);
    }

    public void closeAuction(Auction auction) {
        auction.setStatus(AuctionStatus.ENDED);

        FileService.save("auctions.dat", auctions);
    }

    public List<Auction> getAllAuctions() {
        return auctions;
    }
}
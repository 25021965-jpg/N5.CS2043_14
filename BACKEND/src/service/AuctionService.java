package service;

import model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuctionService {

    private List<Auction> auctions = Collections.synchronizedList(new ArrayList<>());

    public AuctionService() {

        // load từ file
        List<Auction> loaded = FileService.load("auctions.dat");

        if (loaded != null && !loaded.isEmpty()) {
            auctions.addAll(loaded);
        }

        if (auctions.isEmpty()) {
            for (int i = 1; i <= 3; i++) {
                Auction a = new Auction();
                a.setId(String.valueOf(i));
                a.setCurrentPrice(BigDecimal.valueOf(100 * i));
                a.setMinIncrement(BigDecimal.TEN);
                a.setStatus(AuctionStatus.ACTIVE);

                auctions.add(a);
            }

            FileService.save("auctions.dat", auctions);
        }
    }

    public Auction createAuction(User seller, Item item, BigDecimal startPrice) {

        if (seller == null || !seller.hasRole(Role.SELLER)) {
            throw new RuntimeException("User is not seller");
        }

        Auction auction = new Auction();

        String newId = generateId();
        auction.setId(newId);

        auction.setSeller(seller);
        auction.setItem(item);
        auction.setCurrentPrice(startPrice);
        auction.setMinIncrement(BigDecimal.TEN);
        auction.setStatus(AuctionStatus.ACTIVE);

        auctions.add(auction);
        FileService.save("auctions.dat", auctions);

        return auction;
    }

    public List<Auction> getAllAuctions() {
        return auctions;
    }

    // Get bằng id
    public Auction getAuctionById(String id) {

        for (Auction a : auctions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }

        return null;
    }

    public void closeAuction(String id) {
        Auction auction = getAuctionById(id);

        if (auction == null) {
            throw new RuntimeException("Auction not found");
        }

        auction.setStatus(AuctionStatus.ENDED);
        FileService.save("auctions.dat", auctions);
    }

    public void saveAll() {
        FileService.save("auctions.dat", auctions);
    }

    private String generateId() {
        return String.valueOf(auctions.size() + 1);
    }
}
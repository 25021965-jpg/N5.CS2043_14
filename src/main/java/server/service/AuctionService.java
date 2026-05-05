package server.service;

import model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AuctionService {

    private final List<Auction> auctions = new ArrayList<>();
    private static final String FILE = "data/auctions.dat";

    public AuctionService() {

        List<Auction> loaded = FileService.load(FILE);

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

            FileService.save(FILE, auctions);
        }
    }

    //tránh race condition
    public synchronized Auction createAuction(User seller, Item item, BigDecimal startPrice) {

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
        FileService.save(FILE, auctions);

        return auction;
    }

    public synchronized List<Auction> getAllAuctions() {
        return new ArrayList<>(auctions); // tránh sửa trực tiếp
    }

    public synchronized Auction getAuctionById(String id) {
        for (Auction a : auctions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public synchronized void closeAuction(String id) {
        Auction auction = getAuctionById(id);

        if (auction == null) {
            throw new RuntimeException("Auction not found");
        }

        auction.setStatus(AuctionStatus.ENDED);
        FileService.save(FILE, auctions);
    }

    public synchronized void saveAll() {
        FileService.save(FILE, auctions);
    }

    // ID an toàn hơn
    private String generateId() {
        return String.valueOf(System.currentTimeMillis());
    }
}

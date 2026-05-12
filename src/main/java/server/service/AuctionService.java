package server.service;

import model.*;
import server.dao.AuctionDAO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AuctionService {

    private final List<Auction> auctions = new ArrayList<>();
    private final AuctionDAO auctionDAO = new AuctionDAO(); // Khởi tạo DAO

    public AuctionService() {

        List<Auction> loaded = AuctionDAO.findAll();

        if (!loaded.isEmpty()) {
            auctions.addAll(loaded);
        }
    }

    //tránh race condition
    public synchronized Auction createAuction(User seller, Item item, BigDecimal startPrice) {

        if (seller == null || !seller.hasRole(Role.SELLER)) {
            throw new RuntimeException("User is not seller");
        }

        Auction auction = new Auction();
        auction.setId(generateId());
        auction.setSeller(seller);
        auction.setItem(item);
        auction.setCurrentPrice(startPrice);
        auction.setMinIncrement(BigDecimal.TEN);
        auction.setStatus(AuctionStatus.ACTIVE);

        // Lưu vào db trước
        AuctionDAO.save(auction);

        // Sau đó mới thêm vào list bộ nhớ tạm
        auctions.add(auction);

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
        AuctionDAO.updateStatus(id, AuctionStatus.ENDED);
    }

    // ID an toàn hơn
    private String generateId() {
        return String.valueOf(System.currentTimeMillis());
    }
}

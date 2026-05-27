package server.service;

import model.*;
import server.dao.AuctionDAO;
import server.dao.UserDAO;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import static server.dao.AuctionDAO.calculateStatus;

public class AuctionService {

    public static Auction createAuction(
            User seller,
            Item item,
            BigDecimal startPrice,
            BigDecimal minIncrement,
            String startTime,
            String endTime
    ) {
        // 1. đổi role
        if (seller.getRole() == Role.BIDDER) {
            System.out.println("→ User " + seller.getUsername() + " upgraded to SELLER");
        }

        // 2. Khởi tạo Auction
        Auction auction = new Auction();
        auction.setAuction_id(UUID.randomUUID().toString());
        auction.setSeller(seller);
        auction.setItem(item);
        auction.setStartingPrice(startPrice);
        auction.setCurrentPrice(startPrice);
        auction.setMinIncrement(minIncrement);
        auction.setCancelled(false);
        auction.setApproved(false);

        // 3. Parse thời gian
        try {
            auction.setStartTime(LocalDateTime.parse(startTime));
            auction.setEndTime(LocalDateTime.parse(endTime));
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Use yyyy-MM-ddTHH:mm:ss (Ex: 2023-12-31T23:59:59)");
        }

        // 4. Lưu vào DB
        AuctionDAO.save(auction);

        return auction;
    }

    public static List<Auction> getAllAuctions() {
        return AuctionDAO.findAll();
    }

    public static Auction getAuctionById(String id) {

        List<Auction> all = AuctionDAO.findAll();
        if (all == null) return null;

        for (Auction a : all) {
            if (a.getAuction_id().equals(id)) {
                a.setStatus(calculateStatus(a));
                return a;
            }
        }
        return null;
    }

    private static AuctionStatus calculateStatus(Auction a) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (a.isCancelled()) return AuctionStatus.CANCELLED;
        if (!a.isApproved()) return AuctionStatus.PENDING_APPROVAL;
        if (a.getStartTime() != null && now.isBefore(a.getStartTime())) return AuctionStatus.UPCOMING;
        if (a.getEndTime() != null && now.isAfter(a.getEndTime())) return AuctionStatus.ENDED;
        return AuctionStatus.ACTIVE;
    }



    public static List<Auction> getPendingAuctions() {
        return AuctionDAO.findPending();
    }

    public static boolean approveAuction(String auctionId) {
        return AuctionDAO.approveAuction(auctionId);
    }

    public static void cancelAuction(String id) {
        AuctionDAO.cancelAuction(id);
    }
}
package server.service;

import model.*;
import server.dao.AuctionDAO;
import server.dao.UserDAO;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class AuctionService {

    public static Auction createAuction(
            User seller,
            Item item,
            BigDecimal startPrice,
            BigDecimal minIncrement,
            String startTime,
            String endTime
    ) {

        // Auto upgrade BIDDER -> SELLER
        if (seller.getRole() == Role.BIDDER) {

            seller.setRole(Role.SELLER);

            UserDAO userDAO = new UserDAO();

            userDAO.updateRole(
                    seller.getUser_id(),
                    "SELLER"
            );

            System.out.println("→ User upgraded to SELLER");
        }
        // Khởi tạo đối tượng Auction
        Auction auction = new Auction();
        auction.setAuction_id(UUID.randomUUID().toString());
        auction.setSeller(seller);
        auction.setItem(item);
        auction.setStartingPrice(startPrice);
        auction.setCurrentPrice(startPrice);
        auction.setMinIncrement(minIncrement);
        auction.setStatus(AuctionStatus.ACTIVE);

        try {
            auction.setStartTime(java.time.LocalDateTime.parse(startTime));
            auction.setEndTime(java.time.LocalDateTime.parse(endTime));
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Use yyyy-MM-ddTHH:mm:ss");
        }

        // Lưu vào Database thông qua DAO
        // DAO đã xử lý lưu cả Item và Auction
        AuctionDAO.save(auction);

        return auction;
    }

    public static List<Auction> getAllAuctions() {
        return AuctionDAO.findAll();
    }

    public static Auction getAuctionById(String id) {
        // Tìm trong list từ DB
        List<Auction> all = AuctionDAO.findAll();
        for (Auction a : all) {
            if (a.getAuction_id().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public static void cancelAuction(String id) {
        AuctionDAO.cancelAuction(id);
    }
}
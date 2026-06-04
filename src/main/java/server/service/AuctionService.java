package server.service;

import model.*;
import model.Entity.Item.Item;
import model.Entity.User.Role;
import model.Entity.User.User;
import server.dao.AuctionDAO;
import server.dao.UserDAO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionService {
    // Thêm cached
    private static final Map<String, Auction> auctionCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 3000; // 3 giây

    public static Auction createAuction(
            User seller,
            Item item,
            BigDecimal startPrice,
            BigDecimal minIncrement,
            String startTime,
            String endTime
    ) {
        // 1. Log qua polymorphism — seller và item là kiểu cha (User/Item),
        //    getSummary()/getItemDetails() được dispatch đến đúng subclass tại runtime.
        System.out.println("[AuctionService] Seller: " + seller.getSummary());
        System.out.println("[AuctionService] Item:   " + item.getItemDetails());

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
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-ddTHH:mm:ss (Ex: 2023-12-31T23:59:59)");
        }

        // 4. Lưu vào DB
        AuctionDAO.save(auction);

        clearCache();

        return auction;
    }

    public static List<Auction> getAllAuctions() {
        return AuctionDAO.findAll();
    }

    public static Auction getAuctionById(String id) {
        // Kiểm tra cache trước
        Auction cached = auctionCache.get(id);
        if (cached != null) {
            System.out.println("[Cache] Hit - returning cached auction: " + id);
            return cached;
        }

        System.out.println("[Cache] Miss - query DB for auction: " + id);

        // Cache miss, query DB
        Auction a = AuctionDAO.findById(id);
        if (a != null) {
            a.setStatus(calculateStatus(a));
            // Lưu vào cache
            auctionCache.put(id, a);
            // Tự động xóa cache sau 3 giây
            new Timer(true).schedule(new TimerTask() {
                @Override
                public void run() {
                    auctionCache.remove(id);
                    System.out.println("[Cache] Removed auction: " + id);
                }
            }, CACHE_DURATION);
        }
        return a;
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
        boolean result = AuctionDAO.approveAuction(auctionId);
        if (result) {
            // Xóa cache khi duyệt auction
            auctionCache.remove(auctionId);
        }
        return result;
    }

    public static void cancelAuction(String id) {
        AuctionDAO.cancelAuction(id);
        // Xóa cache khi hủy auction
        auctionCache.remove(id);
    }

    private static void clearCache() {
        auctionCache.clear();
        System.out.println("[Cache] Cleared all cache");
    }
}
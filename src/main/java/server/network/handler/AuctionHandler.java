package server.network.handler;

import model.*;
import server.dao.*;
import server.manager.RoomManager;
import server.service.AuctionService;
import server.service.BidService;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AuctionHandler extends BaseHandler {

    private static final Set<String> scheduledAuctions =
            java.util.Collections.synchronizedSet(new HashSet<>());

    public AuctionHandler(User currentUser,
                          PrintWriter writer,
                          String currentAuctionId) {

        super(currentUser, writer, currentAuctionId);
    }

    // ==================== LIST ====================

    public String handleList() {

        List<Auction> auctions = AuctionService.getAllAuctions();

        if (auctions == null || auctions.isEmpty()) {
            return "LIST_EMPTY";
        }

        StringBuilder sb = new StringBuilder("LIST_SUCCESS");

        for (Auction a : auctions) {

            if (a == null || !a.isApproved())
                continue;

            try {

                Item item = a.getItem();

                if (item == null)
                    continue;

                String allImgs = "NO_IMAGE";

                if (item.getImages() != null &&
                        !item.getImages().isEmpty()) {

                    allImgs = String.join(",", item.getImages());
                }

                String itemName =
                        item.getName() != null
                                ? item.getName()
                                  .replace(";", ",")
                                  .replace("|", "-")
                                : "Unnamed";

                String cleanDesc =
                        item.getDescription() != null
                                ? item.getDescription()
                                  .replace(";", ",")
                                  .replace("|", "-")
                                  .replace("\n", " ")
                                : "";

                sb.append("|")
                        .append(a.getAuction_id()).append(";")
                        .append(item.getItem_id()).append(";")
                        .append(itemName).append(";")
                        .append(a.getCurrentPrice()).append(";")
                        .append(a.getMinIncrement()).append(";")
                        .append(allImgs).append(";")
                        .append(a.getStartTime()).append(";")
                        .append(a.getEndTime()).append(";")
                        .append(item.getCategory()).append(";")
                        .append(cleanDesc).append(";")
                        .append(a.getStatus()).append(";")
                        .append(a.getSeller() != null
                                ? a.getSeller().getUser_id()
                                : "");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    // ==================== JOIN ====================

    public String handleJoin(String[] data) {

        if (data.length < 2)
            return "JOIN_FAILED|Invalid auction ID";

        String auctionId = data[1];

        Auction auction =
                AuctionService.getAuctionById(auctionId);

        if (auction == null) {
            return "JOIN_FAILED|Auction not found";
        }

        AuctionStatus status = auction.getStatus();
        if (status != AuctionStatus.ACTIVE) {
            return "JOIN_FAILED|Auction is " + status +
                    " (cannot join)";
        }

        if (currentUser != null
                && auction.getSeller() != null
                && auction.getSeller()
                .getUser_id()
                .equals(currentUser.getUser_id())) {

            return "JOIN_FAILED|You cannot join your own auction";
        }

        this.currentAuctionId = auctionId;

        RoomManager.addClient(
                auctionId,
                writer
        );

        if (auction.getEndTime() != null
                && !scheduledAuctions.contains(auctionId)) {
            scheduledAuctions.add(auctionId);
            long delay = java.time.Duration.between(
                    LocalDateTime.now(), auction.getEndTime()
            ).toMillis();

            if (delay > 0) {
                final String finalAuctionId = auctionId;
                new java.util.Timer(true).schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Auction ended = AuctionService.getAuctionById(finalAuctionId);
                                    if (ended == null) return;

                                    // Lấy danh sách bid của auction
                                    List<model.Bid> bids = server.dao.BidDAO.getBidsByAuctionId(finalAuctionId);

                                    // Xác định winner và final price
                                    model.Bid winningBid = null;
                                    BigDecimal finalPrice = ended.getCurrentPrice();

                                    if (bids != null && !bids.isEmpty()) {
                                        winningBid = bids.get(0); // Bid cao nhất
                                        finalPrice = winningBid.getAmount();
                                    }

                                    // Nếu có winner (có người đặt giá)
                                    if (winningBid != null && winningBid.getBidder() != null) {
                                        User winner = winningBid.getBidder();
                                        User seller = ended.getSeller();
                                        String itemName = ended.getItem() != null ? ended.getItem().getName() : "item";

                                        // Lấy thông tin từ DB
                                        User winnerFromDB = UserDAO.getUserById(winner.getUser_id());
                                        User sellerFromDB = UserDAO.getUserById(seller.getUser_id());
                                        if (winnerFromDB == null || sellerFromDB == null) return;

                                        // ==================== XỬ LÝ WINNER ====================
                                        // Virtual balance của winner đã bị trừ dần trong quá trình bid
                                        // Bây giờ trừ REAL balance của winner (tiền thật)
                                        BigDecimal winnerRealBalance = winnerFromDB.getBalance();
                                        BigDecimal newWinnerRealBalance = winnerRealBalance.subtract(finalPrice);

                                        if (newWinnerRealBalance.compareTo(BigDecimal.ZERO) < 0) {
                                            System.out.println("[AuctionHandler] Winner doesn't have enough real balance!");
                                            return;
                                        }

                                        // Cập nhật real balance
                                        UserDAO.updateBalance(winner.getUser_id(), newWinnerRealBalance);

                                        // Cập nhật virtual balance = real balance mới (vì tiền đã trừ thật)
                                        UserDAO.updateVirtualBalance(winner.getUser_id(), newWinnerRealBalance);

                                        // Ghi transaction cho winner
                                        TransactionDAO.addTransaction(
                                                winner.getUser_id(),
                                                seller.getUser_id(),
                                                finalPrice,
                                                "WIN_BID",
                                                "Won auction: " + itemName
                                        );
                                        System.out.println("[AuctionHandler] Winner " + winner.getUsername() +
                                                " real balance deducted: " + finalPrice +
                                                ", new real balance: " + newWinnerRealBalance);

                                        // Broadcast cập nhật balance cho winner
                                        RoomManager.broadcastToRoomAll(finalAuctionId,
                                                "WINNER_BALANCE|" + newWinnerRealBalance + "|" + winner.getUser_id());

                                        // ==================== XỬ LÝ SELLER ====================
                                        // Seller: cộng tiền thắng bid vào real balance
                                        BigDecimal sellerRealBalance = sellerFromDB.getBalance();
                                        BigDecimal newSellerBalance = sellerRealBalance.add(finalPrice);
                                        UserDAO.updateBalance(seller.getUser_id(), newSellerBalance);
                                        UserDAO.updateVirtualBalance(seller.getUser_id(), newSellerBalance);

                                        // Ghi transaction cho seller
                                        TransactionDAO.addTransaction(
                                                seller.getUser_id(),
                                                winner.getUser_id(),
                                                finalPrice,
                                                "SOLD",
                                                "Sold item: " + itemName
                                        );
                                        System.out.println("[AuctionHandler] Seller " + seller.getUsername() +
                                                " received: " + finalPrice +
                                                ", new balance: " + newSellerBalance);

                                        // Broadcast cập nhật balance cho seller
                                        RoomManager.broadcastToRoomAll(finalAuctionId,
                                                "SELLER_BALANCE|" + newSellerBalance + "|" + seller.getUser_id());

                                        // Broadcast kết quả thắng cuộc
                                        RoomManager.broadcastToRoomAll(finalAuctionId,
                                                "YOU_WON|" + finalPrice + "|" + winner.getUsername());
                                    }

                                    // Broadcast AUCTION_ENDED cho tất cả
                                    RoomManager.broadcastToRoomAll(finalAuctionId, "AUCTION_ENDED");

                                    // Cập nhật status auction thành ENDED
                                    ended.setStatus(AuctionStatus.ENDED);
                                    AuctionDAO.updateAuctionStatus(finalAuctionId, "ENDED");

                                } catch (Exception e) {
                                    System.err.println("[AuctionHandler] Error ending auction: " + e.getMessage());
                                    e.printStackTrace();
                                } finally {
                                    scheduledAuctions.remove(finalAuctionId);
                                }
                            }
                        }, delay);
            }
        }

        return "JOIN_SUCCESS|" + auctionId + "|"
                + auction.getCurrentPrice() + "|"
                + auction.getMinIncrement() + "|"
                + auction.getEndTime();

    }

    // ==================== LEAVE ====================

    public String handleLeave() {

        if (currentAuctionId != null) {

            RoomManager.removeClient(
                    currentAuctionId,
                    writer
            );

            currentAuctionId = null;
        }

        return "LEAVE_SUCCESS";
    }

    // ==================== BID ====================

    public String handleBid(String[] data) {

        if (currentUser == null)
            return "ERROR|Unauthorized";

        if (currentAuctionId == null)
            return "ERROR|You haven't joined any auction";

        try {

            String auctionId = data[1];

            BigDecimal amount =
                    new BigDecimal(data[2]);

            if (!auctionId.equals(currentAuctionId)) {
                return "ERROR|You are not in this room";
            }

            Auction auction =
                    AuctionService.getAuctionById(auctionId);

            if (auction == null)
                return "ERROR|Auction not found";

            String result =
                    BidService.placeBid(
                            currentUser,
                            auction,
                            amount
                    );

            if (result.startsWith("BID_SUCCESS")) {

                String[] parts = result.split("\\|");

                if (parts.length >= 2) {

                    String newPrice = parts[1];

                    String broadcastMsg =
                            "UPDATE_PRICE|"
                                    + auctionId + "|"
                                    + newPrice + "|"
                                    + currentUser.getUsername() + "|"
                                    + LocalDateTime.now().format(
                                    DateTimeFormatter.ofPattern("HH:mm:ss")
                            )+ "|"
                                    + currentUser.getUser_id();

                    RoomManager.broadcastToRoomAll(
                            auctionId,
                            broadcastMsg
                    );
                }
            }

            return result;

        } catch (Exception e) {
            return "BID_FAILED|" + e.getMessage();
        }
    }

    // ==================== BID HISTORY ====================

    public String handleGetBidHistory(String[] data) {

        if (data.length < 2)
            return "BID_HISTORY_FAILED|Invalid auction ID";

        List<Bid> history =
                AuctionDAO.getBidHistory(data[1]);

        if (history.isEmpty()) {
            return "BID_HISTORY_EMPTY";
        }

        StringBuilder sb =
                new StringBuilder("BID_HISTORY_SUCCESS");

        for (Bid bid : history) {

            sb.append("|")
                    .append(bid.getUsername()).append(";")
                    .append(bid.getAmount()).append(";")
                    .append(
                            bid.getTime().format(
                                    DateTimeFormatter.ofPattern("HH:mm:ss")
                            )
                    );
        }

        return sb.toString();
    }

    // ==================== MY AUCTIONS ====================

    public String handleListMyAuctions() {

        if (currentUser == null) {
            return "LIST_MY_AUCTIONS_EMPTY";
        }

        List<Auction> auctions =
                AuctionDAO.findBySeller(
                        currentUser.getUser_id()
                );

        if (auctions.isEmpty()) {
            return "LIST_MY_AUCTIONS_EMPTY";
        }

        StringBuilder sb =
                new StringBuilder("LIST_MY_AUCTIONS_SUCCESS");

        for (Auction a : auctions) {

            Item item = a.getItem();

            String image =
                    (item.getImages() != null
                            && !item.getImages().isEmpty())
                            ? String.join(",", item.getImages())
                            : "";

            sb.append("|")
                    .append(a.getAuction_id()).append(";")
                    .append(item.getItem_id()).append(";")
                    .append(item.getName()).append(";")
                    .append(a.getCurrentPrice()).append(";")
                    .append(a.getMinIncrement()).append(";")
                    .append(image).append(";")
                    .append(a.getStartTime()).append(";")
                    .append(a.getEndTime()).append(";")
                    .append(item.getCategory()).append(";")
                    .append(item.getDescription()).append(";")
                    .append(a.getStatus()).append(";")
                    .append(currentUser.getUser_id());
        }

        return sb.toString();
    }

    // ==================== JOINED AUCTIONS ====================

    public String handleListJoinedAuctions() {

        if (currentUser == null) {
            return "LIST_JOINED_AUCTIONS_EMPTY";
        }

        List<Auction> auctions =
                AuctionDAO.findJoinedAuctions(
                        currentUser.getUser_id()
                );

        if (auctions == null || auctions.isEmpty()) {
            return "LIST_JOINED_AUCTIONS_EMPTY";
        }

        StringBuilder sb =
                new StringBuilder("LIST_JOINED_AUCTIONS_SUCCESS");

        for (Auction a : auctions) {

            Item item = a.getItem();

            String image =
                    (item.getImages() != null
                            && !item.getImages().isEmpty())
                            ? String.join(",", item.getImages())
                            : "";

            sb.append("|")
                    .append(a.getAuction_id()).append(";")
                    .append(item.getItem_id()).append(";")
                    .append(item.getName()).append(";")
                    .append(a.getCurrentPrice()).append(";")
                    .append(a.getMinIncrement()).append(";")
                    .append(image).append(";")
                    .append(a.getStartTime()).append(";")
                    .append(a.getEndTime()).append(";")
                    .append(item.getCategory()).append(";")
                    .append(item.getDescription()).append(";")
                    .append(a.getStatus()).append(";")
                    .append(a.getSeller() != null
                            ? a.getSeller().getUser_id()
                            : "");
        }

        return sb.toString();
    }
}
package server.network.handler;

import model.*;
import server.dao.*;
import server.manager.AuctionTimerManager;
import server.manager.RoomManager;
import server.service.AuctionService;
import server.service.BidService;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AuctionHandler extends BaseHandler {

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

            if (!a.isApproved())
                continue;

            try {

                if (a == null)
                    continue;

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

        if (auction.getEndTime() != null) {
            scheduleAuctionEnd(auctionId, auction.getEndTime());
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
                String newPrice = parts[1];
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                String broadcastMsg = "UPDATE_PRICE|" + auctionId + "|" + newPrice
                        + "|" + currentUser.getUsername() + "|" + time
                        + "|" + currentUser.getUser_id();
                RoomManager.broadcastToRoomAll(auctionId, broadcastMsg);

                // Anti-snipe
                if (parts.length >= 4 && "EXTENDED".equals(parts[2])) {
                    String newEndTime = parts[3];
                    scheduleAuctionEnd(auctionId, LocalDateTime.parse(newEndTime));
                    RoomManager.broadcastToRoomAll(auctionId, "TIME_EXTENDED|" + auctionId + "|" + newEndTime);
                    System.out.println("Broadcasted TIME_EXTENDED: " + newEndTime);
                }

                return "BID_SUCCESS|" + newPrice;
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
    // ==================== SCHEDULE AUCTION END ====================

    public static void scheduleAuctionEnd(String auctionId, LocalDateTime endTime) {
        long delay = java.time.Duration.between(
                LocalDateTime.now(), endTime
        ).toMillis();

        if (delay <= 0) return;

        AuctionTimerManager.scheduleEnd(auctionId, delay, () -> {
            try {
                Auction ended = AuctionService.getAuctionById(auctionId);
                if (ended == null) return;

                List<Bid> bids = BidDAO.getBidsByAuctionId(auctionId);
                Bid winningBid = (bids != null && !bids.isEmpty()) ? bids.get(0) : null;

                if (winningBid != null && winningBid.getBidder() != null) {
                    User winner = winningBid.getBidder();
                    User seller = ended.getSeller();
                    BigDecimal finalPrice = winningBid.getAmount();
                    String itemName = ended.getItem() != null ? ended.getItem().getName() : "item";

                    User winnerFromDB = UserDAO.getUserById(winner.getUser_id());
                    User sellerFromDB = UserDAO.getUserById(seller.getUser_id());

                    if (winnerFromDB != null && sellerFromDB != null) {
                        // Trừ tiền winner
                        BigDecimal winnerNewBalance = winnerFromDB.getBalance().subtract(finalPrice);
                        if (winnerNewBalance.compareTo(BigDecimal.ZERO) >= 0) {
                            UserDAO.updateBalance(winner.getUser_id(), winnerNewBalance);
                            TransactionDAO.addTransaction(
                                    winner.getUser_id(), seller.getUser_id(),
                                    finalPrice, "WIN_BID", "Won auction: " + itemName
                            );
                        }

                        // Cộng tiền seller
                        BigDecimal sellerNewBalance = sellerFromDB.getBalance().add(finalPrice);
                        UserDAO.updateBalance(seller.getUser_id(), sellerNewBalance);
                        TransactionDAO.addTransaction(
                                seller.getUser_id(), winner.getUser_id(),
                                finalPrice, "SOLD", "Sold item: " + itemName
                        );
                    }

                    RoomManager.broadcastToRoomAll(auctionId, "YOU_WON|" + finalPrice + "|" + winner.getUsername());
                }

                RoomManager.broadcastToRoomAll(auctionId, "AUCTION_ENDED");
                AuctionDAO.updateAuctionStatus(auctionId, "ENDED");

            } catch (Exception e) {
                System.err.println("[AuctionTimerManager] Error ending auction: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
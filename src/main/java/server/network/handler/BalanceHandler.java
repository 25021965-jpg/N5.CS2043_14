package server.network.handler;

import model.Transaction;
import model.Bid;
import model.Auction;
import model.Entity.User.User;
import server.dao.TransactionDAO;
import server.dao.UserDAO;
import server.dao.BidDAO;
import server.manager.RoomManager;
import server.service.AuctionService;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

public class BalanceHandler extends BaseHandler {


    public BalanceHandler(User currentUser,
                          PrintWriter writer,
                          String currentAuctionId) {

        super(currentUser, writer, currentAuctionId);
    }

    // ==================== GET BALANCE ====================

    public String handleGetBalance(String[] data) {
        if (currentUser == null)
            return "ERROR|Not logged in";

        String userId = data.length >= 2 ? data[1] : currentUser.getUser_id();
        User user = UserDAO.getUserById(userId);

        if (user != null) {
            return "BALANCE_UPDATE_SUCCESS|" + user.getBalance();
        }
        return "BALANCE_UPDATE_FAILED|User not found";
    }

    // ==================== DEPOSIT ====================

    public String handleDeposit(String[] data) {
        if (currentUser == null)
            return "BALANCE_UPDATE_FAILED|Not logged in";

        if (data.length < 3)
            return "BALANCE_UPDATE_FAILED|Missing amount";

        String targetUserId = data[1];
        if (!targetUserId.equals(currentUser.getUser_id())) {
            return "BALANCE_UPDATE_FAILED|Cannot deposit to another user";
        }

        try {
            BigDecimal amount = new BigDecimal(data[2]);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return "BALANCE_UPDATE_FAILED|Amount must be greater than 0";
            }

            User user = UserDAO.getUserById(targetUserId);
            if (user == null) {
                return "BALANCE_UPDATE_FAILED|User not found";
            }

            // Cộng account balance
            BigDecimal newBalance = user.getBalance().add(amount);
            boolean saved = UserDAO.updateBalance(targetUserId, newBalance);

            if (saved) {
                // Cộng virtual balance cùng số tiền
                UserDAO.addVirtualBalance(targetUserId, amount);

                TransactionDAO.addTransaction(targetUserId, null, amount, "DEPOSIT", "Deposit money");
                currentUser.setBalance(newBalance);

                // Lấy virtual balance mới để trả về
                BigDecimal newVirtual = UserDAO.getVirtualBalance(targetUserId);

                return "BALANCE_UPDATE_SUCCESS|" + newBalance + "|" + newVirtual;
            } else {
                return "BALANCE_UPDATE_FAILED|Database error";
            }

        } catch (NumberFormatException e) {
            return "BALANCE_UPDATE_FAILED|Invalid amount";
        }
    }

    // ==================== WITHDRAW ====================

    public String handleWithdraw(String[] data) {
        if (currentUser == null)
            return "BALANCE_UPDATE_FAILED|Not logged in";

        if (data.length < 3)
            return "BALANCE_UPDATE_FAILED|Missing amount";

        String targetUserId = data[1];
        if (!targetUserId.equals(currentUser.getUser_id())) {
            return "BALANCE_UPDATE_FAILED|Cannot withdraw from another user";
        }

        try {
            BigDecimal amount = new BigDecimal(data[2]);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return "BALANCE_UPDATE_FAILED|Amount must be greater than 0";
            }

            User user = UserDAO.getUserById(targetUserId);
            if (user == null) {
                return "BALANCE_UPDATE_FAILED|User not found";
            }

            if (user.getBalance().compareTo(amount) < 0) {
                return "BALANCE_UPDATE_FAILED|Insufficient balance";
            }

            // Trừ account balance
            BigDecimal newBalance = user.getBalance().subtract(amount);
            boolean saved = UserDAO.updateBalance(targetUserId, newBalance);

            if (saved) {
                // Trừ virtual balance cùng số tiền
                UserDAO.deductVirtualBalance(targetUserId, amount);

                TransactionDAO.addTransaction(targetUserId, null, amount, "WITHDRAW", "Withdraw money");
                currentUser.setBalance(newBalance);

                // Lấy virtual balance mới để trả về
                BigDecimal newVirtual = UserDAO.getVirtualBalance(targetUserId);

                return "BALANCE_UPDATE_SUCCESS|" + newBalance + "|" + newVirtual;
            } else {
                return "BALANCE_UPDATE_FAILED|Database error";
            }

        } catch (NumberFormatException e) {
            return "BALANCE_UPDATE_FAILED|Invalid amount";
        }
    }

    // ==================== TRANSACTIONS ====================

    public String handleGetTransactions(String[] data) {
        if (currentUser == null)
            return "ERROR|Not logged in";

        String userId = data.length >= 2 ? data[1] : currentUser.getUser_id();
        if (!userId.equals(currentUser.getUser_id())) {
            return "ERROR|Permission denied";
        }

        List<Transaction> transactions = TransactionDAO.getTransactionsByUserId(userId);
        if (transactions.isEmpty()) {
            return "TRANSACTIONS_LIST|";
        }

        StringBuilder sb = new StringBuilder("TRANSACTIONS_LIST|");
        for (Transaction tx : transactions) {
            sb.append(tx.getType()).append(";")
                    .append(tx.getAmount()).append(";")
                    .append(tx.getFormattedTime()).append(";")
                    .append(tx.getDescription() == null ? "" : tx.getDescription())
                    .append("|");
        }
        return sb.toString();
    }

    //  ATOMIC ADD - AN TOÀN
    public String handleAddVirtualBalance(String[] data) {
        if (currentUser == null) return "ERROR|Not logged in";
        if (data.length < 3) return "ERROR|Missing data";

        String userId = data[1];
        BigDecimal amount = new BigDecimal(data[2]);

        if (!userId.equals(currentUser.getUser_id())) {
            return "ERROR|Permission denied";
        }

        boolean success = UserDAO.addVirtualBalance(userId, amount);

        if (success) {
            BigDecimal newBalance = UserDAO.getVirtualBalance(userId);
            System.out.println("[BalanceHandler] Added " + amount + " to user " + userId + ", new balance: " + newBalance);
            return "VIRTUAL_BALANCE_UPDATED|" + newBalance;
        } else {
            return "ERROR|Failed to add virtual balance";
        }
    }

    // ==================== PAY AUCTION (TRANSFER) ====================
    public String handlePayAuction(String[] data) {
        if (currentUser == null) return "PAY_AUCTION_FAILED|Not logged in";
        if (data.length < 2) return "PAY_AUCTION_FAILED|Missing auction id";

        String auctionId = data[1];

        // Find highest bid
        Bid highest = server.dao.BidDAO.getHighestBid(auctionId);
        if (highest == null || highest.getBidder() == null) {
            return "PAY_AUCTION_FAILED|No winner for this auction";
        }

        String winnerId = highest.getBidder().getUser_id();
        if (!winnerId.equals(currentUser.getUser_id())) {
            return "PAY_AUCTION_FAILED|You are not the winner";
        }

        BigDecimal amount = highest.getAmount();

        // Get auction to find seller
        Auction auction = AuctionService.getAuctionById(auctionId);
        if (auction == null || auction.getSeller() == null) {
            return "PAY_AUCTION_FAILED|Auction or seller not found";
        }

        String sellerId = auction.getSeller().getUser_id();

        // Load users
        User winner = UserDAO.getUserById(winnerId);
        User seller = UserDAO.getUserById(sellerId);

        if (winner == null || seller == null) {
            return "PAY_AUCTION_FAILED|User(s) not found";
        }

        if (winner.getBalance().compareTo(amount) < 0) {
            return "PAY_AUCTION_FAILED|Insufficient balance";
        }

        // Deduct winner
        BigDecimal newWinner = winner.getBalance().subtract(amount);
        boolean wSaved = UserDAO.updateBalance(winnerId, newWinner);
        if (!wSaved) return "PAY_AUCTION_FAILED|DB error (winner)";

        // Credit seller
        BigDecimal newSeller = seller.getBalance().add(amount);
        boolean sSaved = UserDAO.updateBalance(sellerId, newSeller);
        if (!sSaved) {
            // try rollback winner update (best-effort)
            UserDAO.updateBalance(winnerId, winner.getBalance());
            return "PAY_AUCTION_FAILED|DB error (seller)";
        }

        // Create transactions
        TransactionDAO.addTransaction(winnerId, sellerId, amount, "TRANSFER_OUT", seller.getUsername());
        TransactionDAO.addTransaction(sellerId, winnerId, amount, "TRANSFER_IN", winner.getUsername());

        // Notify room members about new balances (if auction room exists)
        RoomManager.broadcastToRoomAll(auctionId, "WINNER_BALANCE|" + newWinner + "|" + winnerId);
        RoomManager.broadcastToRoomAll(auctionId, "SELLER_BALANCE|" + newSeller + "|" + sellerId);

        // Return success to requester
        return "PAY_AUCTION_SUCCESS|" + auctionId + "|" + newWinner;
    }
}
package server.network.handler;

import model.*;
import model.Entity.User.Role;
import model.Entity.User.User;
import server.dao.*;

import java.io.PrintWriter;
import java.util.List;

public class AdminHandler extends BaseHandler {

    public AdminHandler(User currentUser,
                        PrintWriter writer,
                        String currentAuctionId) {

        super(currentUser, writer, currentAuctionId);
    }

    // ==================== CHECK ADMIN ====================

    private boolean isAdmin() {

        return currentUser != null
                && currentUser.getRole() == Role.ADMIN;
    }

    // ==================== USERS ====================

    public String handleListUsers() {

        if (!isAdmin())
            return "ERROR|Permission denied";

        List<User> users = UserDAO.findAll();

        if (users == null || users.isEmpty()) {
            return "USER_LIST_EMPTY";
        }

        StringBuilder sb =
                new StringBuilder("USER_LIST_SUCCESS");

        for (User u : users) {

            sb.append("|")
                    .append(u.getUser_id()).append(";")
                    .append(u.getUsername()).append(";")
                    .append(u.getEmail()).append(";")
                    .append(u.getRole().name());
        }

        return sb.toString();
    }

    public String handleDeleteUser(String[] data) {

        if (!isAdmin())
            return "ERROR|Permission denied";

        if (data.length < 2)
            return "DELETE_USER_FAILED|Missing ID";

        String targetId = data[1];

        if (targetId.equals(currentUser.getUser_id())) {
            return "DELETE_USER_FAILED|Cannot delete yourself";
        }

        boolean success =
                UserDAO.deleteUser(targetId);

        return success
                ? "DELETE_USER_SUCCESS"
                : "DELETE_USER_FAILED";
    }

    public String handleUpdateUserRole(String[] data) {

        if (!isAdmin())
            return "ERROR|Permission denied";

        if (data.length < 3)
            return "UPDATE_USER_ROLE_FAILED";

        boolean success =
                UserDAO.updateUserRole(
                        data[1],
                        data[2]
                );

        return success
                ? "UPDATE_USER_ROLE_SUCCESS"
                : "UPDATE_USER_ROLE_FAILED";
    }

    // ==================== ITEMS ====================

    public String handleListItems() {

        if (!isAdmin())
            return "ERROR|Permission denied";

        List<String[]> items =
                ItemDAO.findAllWithSeller();

        if (items.isEmpty()) {
            return "ITEM_LIST_EMPTY";
        }

        StringBuilder sb =
                new StringBuilder("ITEM_LIST_SUCCESS");

        for (String[] row : items) {

            sb.append("|")
                    .append(String.join(";", row));
        }

        return sb.toString();
    }

    public String handleDeleteItem(String[] data) {

        if (!isAdmin())
            return "ERROR|Permission denied";

        if (data.length < 2)
            return "DELETE_ITEM_FAILED|Missing ID";

        boolean ok =
                ItemDAO.deleteItem(data[1]);

        return ok
                ? "DELETE_ITEM_SUCCESS"
                : "DELETE_ITEM_FAILED";
    }

    public String handleUpdateItem(String[] data) {

        if (!isAdmin())
            return "ERROR|Permission denied";

        if (data.length < 5)
            return "UPDATE_ITEM_FAILED|Missing data";

        boolean ok =
                ItemDAO.updateItem(
                        data[1],
                        data[2],
                        data[3],
                        data[4]
                );

        return ok
                ? "UPDATE_ITEM_SUCCESS"
                : "UPDATE_ITEM_FAILED";
    }

    // ==================== AUCTION HISTORY ====================

    public String handleListAuctionHistory() {

        if (!isAdmin())
            return "ERROR|Permission denied";

        List<String[]> history =
                AuctionDAO.findAuctionHistory();

        if (history.isEmpty()) {
            return "AUCTION_HISTORY_EMPTY";
        }

        StringBuilder sb =
                new StringBuilder("AUCTION_HISTORY_SUCCESS");

        for (String[] row : history) {

            sb.append("|")
                    .append(String.join(";", row));
        }

        return sb.toString();
    }

    // ==================== ALL AUCTIONS ====================

    public String handleListAllAuctions() {

        if (!isAdmin())
            return "ERROR|Permission denied";

        List<String[]> auctions =
                AuctionDAO.findAllAsStrings();

        if (auctions.isEmpty()) {
            return "ALL_AUCTIONS_EMPTY";
        }

        StringBuilder sb =
                new StringBuilder("ALL_AUCTIONS_SUCCESS");

        for (String[] row : auctions) {

            sb.append("|")
                    .append(String.join(";", row));
        }

        return sb.toString();
    }

    // ==================== STOP AUCTION ====================

    public String handleStopAuction(String[] data) {

        if (!isAdmin())
            return "ERROR|Permission denied";

        if (data.length < 2)
            return "STOP_AUCTION_FAILED|Missing ID";

        boolean ok =
                AuctionDAO.stopAuction(data[1]);

        return ok
                ? "STOP_AUCTION_SUCCESS"
                : "STOP_AUCTION_FAILED";
    }

    // ==================== RESUME AUCTION ====================

    public String handleResumeAuction(String[] data) {

        if (!isAdmin())
            return "ERROR|Permission denied";

        if (data.length < 2)
            return "RESUME_AUCTION_FAILED|Missing ID";

        boolean ok =
                AuctionDAO.resumeAuction(data[1]);

        return ok
                ? "RESUME_AUCTION_SUCCESS"
                : "RESUME_AUCTION_FAILED";
    }

    // ==================== CANCEL AUCTION ====================

    public String handleCancelAuction(String[] data) {

        if (!isAdmin())
            return "ERROR|Permission denied";

        if (data.length < 2)
            return "CANCEL_AUCTION_FAILED|Missing ID";

        AuctionDAO.cancelAuction(data[1]);

        return "CANCEL_AUCTION_SUCCESS";
    }

    // ==================== APPROVE AUCTION ====================

    public String handleApproveAuction(String[] data) {

        if (!isAdmin())
            return "APPROVE_AUCTION_FAILED|Permission denied";

        if (data.length < 2)
            return "APPROVE_AUCTION_FAILED|Missing auction id";

        boolean ok =
                AuctionDAO.approveAuction(data[1]);

        return ok
                ? "APPROVE_AUCTION_SUCCESS"
                : "APPROVE_AUCTION_FAILED|DB error";
    }

    // ==================== PENDING AUCTIONS ====================

    public String handleListPendingAuctions() {

        if (!isAdmin())
            return "PENDING_AUCTIONS_EMPTY";

        List<Auction> pending =
                AuctionDAO.findPending();

        if (pending == null || pending.isEmpty()) {
            return "PENDING_AUCTIONS_EMPTY";
        }

        StringBuilder sb =
                new StringBuilder("PENDING_AUCTIONS_SUCCESS");

        for (Auction a : pending) {

            sb.append("|")
                    .append(a.getAuction_id()).append(";")
                    .append(
                            a.getItem() != null
                                    ? a.getItem().getName()
                                    : ""
                    ).append(";")
                    .append(
                            a.getItem() != null
                                    ? a.getItem().getCategory()
                                    : ""
                    ).append(";")
                    .append(
                            a.getSeller() != null
                                    ? a.getSeller().getUsername()
                                    : ""
                    ).append(";")
                    .append("PENDING_APPROVAL");
        }

        return sb.toString();
    }
}
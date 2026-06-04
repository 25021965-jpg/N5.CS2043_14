package server.network.handler;

import model.Entity.Item.Item;
import model.Factory.ItemFactory;
import model.Entity.User.User;
import server.manager.RoomManager;
import server.service.AuctionService;
import server.service.AuthService;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AuthHandler extends BaseHandler {

    public AuthHandler(User currentUser,
                       PrintWriter writer,
                       String currentAuctionId) {

        super(currentUser, writer, currentAuctionId);
    }

    // ==================== LOGIN ====================

    public String handleLogin(String[] data) {

        if (data.length < 3)
            return "LOGIN_FAILED";

        User user = AuthService.login(
                data[1].trim(),
                data[2].trim()
        );

        if (user != null) {

            this.currentUser = user;

            return "LOGIN_SUCCESS|"
                    + user.getUser_id() + "|"
                    + user.getFullname() + "|"
                    + user.getUsername() + "|"
                    + user.getEmail() + "|"
                    + (user.getDob() == null
                    ? ""
                    : user.getDob()) + "|"
                    + user.getRole().name() + "|"
                    + user.getBalance();
        }

        return "LOGIN_FAILED";
    }

    // ==================== REGISTER ====================

    public String handleRegister(String[] data) {

        if (data.length < 6)
            return "REGISTER_FAILED";

        User user = AuthService.register(
                data[1],
                data[2],
                data[3],
                data[4],
                data[5]
        );

        return user != null
                ? "REGISTER_SUCCESS"
                : "REGISTER_FAILED|Exists";
    }

    // ==================== FORGOT PASSWORD ====================

    public String handleForgotPassword(String[] data) {

        if (data.length < 6)
            return "FORGOT_FAILED|Missing information";

        boolean success =
                AuthService.resetPassword(
                        data[1].trim(),
                        data[2].trim(),
                        data[3].trim(),
                        data[4].trim(),
                        data[5].trim()
                );

        return success
                ? "FORGOT_SUCCESS"
                : "FORGOT_FAILED|Information does not match";
    }

    // ==================== LOGOUT ====================

    public String handleLogout() {

        if (currentAuctionId != null) {

            RoomManager.removeClient(
                    currentAuctionId,
                    writer
            );

            currentAuctionId = null;
        }

        currentUser = null;

        return "LOGOUT_SUCCESS";
    }

    // ==================== DELETE ACCOUNT ====================

    public String handleDeleteAccount(String[] data) {

        if (currentUser == null) return "DELETE_USER_FAILED|Not logged in";

        String userId = currentUser.getUser_id();

        boolean ok = server.dao.UserDAO.deleteUser(userId);

        if (ok) {
            // After deletion, clear currentUser reference
            currentUser = null;
            return "DELETE_USER_SUCCESS";
        } else {
            return "DELETE_USER_FAILED|Database error";
        }
    }

    // ==================== CREATE AUCTION ====================

    public String handleCreate(String[] data) {

        if (data.length < 11)
            return "CREATE_FAILED|Invalid data";

        try {

            if (currentUser == null)
                return "ERROR|Unauthorized";

            Item item = ItemFactory.createFromCategory(data[5]);
            item.setItem_id(data[2]);
            item.setName(data[3]);
            item.setDescription(data[4]);

            List<String> imageList =
                    new ArrayList<>();

            if (!data[6].isEmpty()
                    && !data[6].equals("NO_IMAGE")) {

                imageList.addAll(
                        List.of(data[6].split(","))
                );
            }

            item.setImages(imageList);

            // XÓA phần update role

            AuctionService.createAuction(
                    currentUser,
                    item,
                    new BigDecimal(data[7]),
                    new BigDecimal(data[8]),
                    data[9],
                    data[10]
            );

            return "CREATE_SUCCESS";

        } catch (Exception e) {
            return "CREATE_FAILED|" + e.getMessage();
        }
    }
}
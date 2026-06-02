package server.network.handler;

import model.User;
import server.dao.UserDAO;

import java.io.PrintWriter;
import java.math.BigDecimal;

public abstract class BaseHandler {

    protected User currentUser;
    protected PrintWriter writer;
    protected String currentAuctionId;

    public BaseHandler(User currentUser,
                       PrintWriter writer,
                       String currentAuctionId) {

        this.currentUser = currentUser;
        this.writer = writer;
        this.currentAuctionId = currentAuctionId;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentAuctionId() {
        return currentAuctionId;
    }

    public String handleGetVirtualBalance(String[] data) {
        if (currentUser == null) return "ERROR|Not logged in";
        String userId = data.length >= 2 ? data[1] : currentUser.getUser_id();

        BigDecimal vb = UserDAO.getVirtualBalance(userId);

        if (vb == null || vb.compareTo(BigDecimal.ZERO) == 0) {
            User user = UserDAO.getUserById(userId);
            if (user != null) {
                vb = user.getBalance();
                if (vb != null && vb.compareTo(BigDecimal.ZERO) > 0) {
                    UserDAO.updateVirtualBalance(userId, vb);
                }
            }
            if (vb == null) vb = BigDecimal.ZERO;
        }

        return "VIRTUAL_BALANCE|" + vb;
    }
}


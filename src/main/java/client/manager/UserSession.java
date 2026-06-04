package client.manager;

import model.Entity.User.User;

import java.math.BigDecimal;

public class UserSession {
    private static volatile User currentUser;
    private static volatile BigDecimal virtualBalance;
    private static volatile BigDecimal pendingBid;
    private static volatile boolean autoBidActive = false;
    private static volatile BigDecimal autoBidMax = null;
    private static volatile String autoBidAuctionId = null;
    private static volatile String autoBidUserId = null;


    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {currentUser = user;}

    public static void clear() {
        currentUser = null;
        virtualBalance = null;
        pendingBid = null;
    }

    public static BigDecimal getVirtualBalance() {return virtualBalance;}

    public static void setVirtualBalance(BigDecimal balance) {virtualBalance = balance;}

    public static BigDecimal getPendingBid() {return pendingBid;}

    public static void setPendingBid(BigDecimal bid) {
        pendingBid = bid;
    }

    public static void setAutoBid(String auctionId, BigDecimal max) {
        autoBidActive = true;
        autoBidAuctionId = auctionId;
        autoBidMax = max;
        autoBidUserId = (currentUser != null) ? currentUser.getUser_id() : null;

    }
    public static void clearAutoBid() {
        autoBidActive = false;
        autoBidAuctionId = null;
        autoBidMax = null;
        autoBidUserId = null;

    }
    public static boolean isAutoBidActive() { return autoBidActive; }
    public static BigDecimal getAutoBidMax() { return autoBidMax; }
    public static String getAutoBidAuctionId() { return autoBidAuctionId; }
    public static String getAutoBidUserId() { return autoBidUserId; }


}
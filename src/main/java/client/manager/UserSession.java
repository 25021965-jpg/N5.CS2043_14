package client.manager;

import model.User;

import java.math.BigDecimal;

public class UserSession {
    private static User currentUser;
    private static BigDecimal virtualBalance;
    private static BigDecimal pendingBid;

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
}
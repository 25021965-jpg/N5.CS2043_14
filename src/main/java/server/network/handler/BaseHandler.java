package server.network.handler;

import model.User;

import java.io.PrintWriter;

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
}

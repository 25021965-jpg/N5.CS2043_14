package common;

public enum Command {

    // ================= AUTH =================
    LOGIN,
    REGISTER,
    LOGOUT,
    FORGOT_PASSWORD,

    // ================= USER AUCTION =================
    LIST,
    CREATE,
    JOIN,
    LEAVE,
    BID,
    GET_BID_HISTORY,
    GET_VIRTUAL_BALANCE,

    // ================= USER PROFILE =================
    GET_PROFILE,
    GET_BALANCE,
    GET_TRANSACTIONS,
    DEPOSIT,
    WITHDRAW,

    // ================= USER FAVOURITE =================
    ADD_FAVOURITE,
    REMOVE_FAVOURITE,
    LIST_FAVOURITES,

    // ================= USER AUCTION MANAGEMENT =================
    LIST_CREATED_AUCTIONS,
    LIST_JOINED_AUCTIONS,

    // ================= ADMIN USER =================
    LIST_USERS,
    DELETE_USER,
    UPDATE_USER_ROLE,

    // ================= ADMIN ITEM =================
    LIST_ITEMS,
    DELETE_ITEM,
    UPDATE_ITEM,

    // ================= ADMIN AUCTION =================
    LIST_AUCTION_HISTORY,
    LIST_ALL_AUCTIONS,
    STOP_AUCTION,
    RESUME_AUCTION,
    CANCEL_AUCTION,
    APPROVE_AUCTION,
    LIST_PENDING_AUCTIONS,

    // ================= AUTO BID =================
    SET_AUTO_BID,
    CANCEL_AUTO_BID;

    public static Command from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Command.valueOf(value.trim().toUpperCase());

        } catch (Exception e) {
            System.err.println("Unknown Command: " + value);

            return null;
        }
    }
}
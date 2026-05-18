package common;

public enum Command {
    LOGIN,
    REGISTER,
    LOGOUT,
    CREATE,
    JOIN,
    LIST,
    LIST_USERS,
    DELETE_USER,
    FORGOT_PASSWORD,
    BID,
    GET_PROFILE,
    LIST_ITEMS,
    DELETE_ITEM,
    UPDATE_ITEM,
    LIST_AUCTION_HISTORY,
    LIST_ALL_AUCTIONS,
    STOP_AUCTION,
    RESUME_AUCTION,
    CANCEL_AUCTION;



    // parse an toàn (tránh crash)
    public static Command from(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Command.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}

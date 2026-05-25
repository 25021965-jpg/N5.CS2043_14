package common;

public enum Command {
    LOGIN,
    REGISTER,
    LOGOUT,
    CREATE,
    JOIN,
    LEAVE,
    LIST,
    LIST_USERS,
    DELETE_USER,
    FORGOT_PASSWORD,
    BID,
    GET_PROFILE,
    ADD_FAVOURITE,
    REMOVE_FAVOURITE,
    LIST_FAVOURITES,
    LIST_MY_AUCTIONS,
    GET_BALANCE,
    LIST_ITEMS,
    DELETE_ITEM,
    UPDATE_ITEM,
    GET_BID_HISTORY,
    LIST_AUCTION_HISTORY,
    LIST_ALL_AUCTIONS,
    STOP_AUCTION,
    RESUME_AUCTION,
    CANCEL_AUCTION, GET_TRANSACTIONS, WITHDRAW, DEPOSIT,
    APPROVE_AUCTION,
    LIST_PENDING_AUCTIONS;

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

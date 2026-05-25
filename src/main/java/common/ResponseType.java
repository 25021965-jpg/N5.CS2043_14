package common;

import java.util.Locale;

public enum ResponseType {

    // ================= AUTH =================
    LOGIN_SUCCESS,
    LOGIN_FAILED,

    REGISTER_SUCCESS,
    REGISTER_FAILED,

    FORGOT_SUCCESS,
    FORGOT_FAILED,

    DISCONNECTED,
    ERROR,

    // ================= USER AUCTION =================
    CREATE_SUCCESS,
    CREATE_FAILED,

    LIST_SUCCESS,
    LIST_EMPTY,

    JOIN_SUCCESS,
    JOIN_FAILED,

    BID_SUCCESS,
    BID_FAILED,

    UPDATE_PRICE,

    BID_HISTORY_SUCCESS,
    BID_HISTORY_EMPTY,

    // ================= USER FAVOURITE =================
    LIST_FAVOURITES_SUCCESS,
    LIST_FAVOURITES_EMPTY,

    ADD_FAVOURITE_SUCCESS,
    ADD_FAVOURITE_FAILED,

    REMOVE_FAVOURITE_SUCCESS,
    REMOVE_FAVOURITE_FAILED,

    // ================= USER AUCTION HISTORY =================
    LIST_MY_AUCTIONS_SUCCESS,
    LIST_MY_AUCTIONS_EMPTY,

    LIST_JOINED_AUCTIONS_SUCCESS,
    LIST_JOINED_AUCTIONS_EMPTY,

    // ================= USER BALANCE =================
    BALANCE_UPDATE_SUCCESS,
    BALANCE_UPDATE_FAILED,

    TRANSACTION_HISTORY_SUCCESS,

    // ================= ADMIN USER =================
    USER_LIST_SUCCESS,
    USER_LIST_EMPTY,

    DELETE_USER_SUCCESS,
    DELETE_USER_FAILED,

    UPDATE_USER_ROLE_SUCCESS,
    UPDATE_USER_ROLE_FAILED,

    // ================= ADMIN ITEM =================
    ITEM_LIST_SUCCESS,
    ITEM_LIST_EMPTY,

    DELETE_ITEM_SUCCESS,
    DELETE_ITEM_FAILED,

    UPDATE_ITEM_SUCCESS,
    UPDATE_ITEM_FAILED,

    // ================= ADMIN AUCTION =================
    AUCTION_HISTORY_SUCCESS,
    AUCTION_HISTORY_EMPTY,

    ALL_AUCTIONS_SUCCESS,
    ALL_AUCTIONS_EMPTY,

    STOP_AUCTION_SUCCESS,
    STOP_AUCTION_FAILED,

    RESUME_AUCTION_SUCCESS,
    RESUME_AUCTION_FAILED,

    CANCEL_AUCTION_SUCCESS,
    CANCEL_AUCTION_FAILED,

    PENDING_AUCTIONS_SUCCESS,
    PENDING_AUCTIONS_EMPTY,

    APPROVE_AUCTION_SUCCESS,
    APPROVE_AUCTION_FAILED;

    public static ResponseType from(String msg) {
        if (msg == null || msg.isBlank()) {
            return null;
        }

        try {
            String key =
                    msg.split("\\|")[0]
                            .trim()
                            .toUpperCase(Locale.ROOT);

            return ResponseType.valueOf(key);

        } catch (Exception e) {
            System.err.println("✕ Unknown Response Type: " + msg);

            return null;
        }
    }
}
package common;

import java.util.Locale;

public enum ResponseType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    REGISTER_SUCCESS,
    REGISTER_FAILED,
    CREATE_SUCCESS,
    CREATE_FAILED,
    JOIN_SUCCESS,
    JOIN_FAILED,
    LIST_SUCCESS,
    LIST_EMPTY,
    BID_SUCCESS,
    BID_FAILED,
    USER_LIST_SUCCESS,
    USER_LIST_EMPTY,
    DELETE_USER_SUCCESS,
    DELETE_USER_FAILED,
    ERROR,
    DISCONNECTED,
    FORGOT_SUCCESS,
    FORGOT_FAILED,
    ITEM_LIST_SUCCESS,
    ITEM_LIST_EMPTY,
    DELETE_ITEM_SUCCESS,
    DELETE_ITEM_FAILED,
    UPDATE_ITEM_SUCCESS,
    UPDATE_ITEM_FAILED,
    BID_HISTORY_SUCCESS,
    BID_HISTORY_EMPTY,
    UPDATE_PRICE,
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
    LIST_FAVOURITES_SUCCESS,
    LIST_FAVOURITES_EMPTY,
    ADD_FAVOURITE_SUCCESS,
    ADD_FAVOURITE_FAILED,
    REMOVE_FAVOURITE_SUCCESS,
    REMOVE_FAVOURITE_FAILED,
    PENDING_AUCTIONS_SUCCESS,
    PENDING_AUCTIONS_EMPTY,
    APPROVE_AUCTION_SUCCESS,
    APPROVE_AUCTION_FAILED;

    public static ResponseType from(String msg) {
        if (msg == null || msg.isBlank()) return null;
        try {
            // Lấy phần đầu trước dấu |
            String key = msg.split("\\|")[0].trim().toUpperCase(Locale.ROOT);            return ResponseType.valueOf(key);
        } catch (Exception e) {
            // In log nhẹ để debug
            System.err.println("✕ Unknown Response Type: " + msg);
            return null;
        }
    }
}
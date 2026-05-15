package common;

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
    DISCONNECTED;

    public static ResponseType from(String msg) {
        if (msg == null || msg.isBlank()) return null;
        try {
            // Lấy phần đầu trước dấu |
            String key = msg.split("\\|")[0].trim().toUpperCase();
            return ResponseType.valueOf(key);
        } catch (Exception e) {
            // In log nhẹ để debug
            System.err.println("✕ Unknown Response Type: " + msg);
            return null;
        }
    }
}
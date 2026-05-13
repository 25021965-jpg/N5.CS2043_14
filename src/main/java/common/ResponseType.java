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
    ERROR,
    DISCONNECTED;

    public static ResponseType from(String msg) {
        try {
            String key = msg.split("\\|")[0].toUpperCase();
            return ResponseType.valueOf(key);
        } catch (Exception e) {
            return null;
        }
    }
}

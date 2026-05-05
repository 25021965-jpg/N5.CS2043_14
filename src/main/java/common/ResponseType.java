package common;

public enum ResponseType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    ERROR,
    DISCONNECTED;

    public static ResponseType from(String msg) {
        try {
            String key = msg.split(" ")[0].toUpperCase();
            return ResponseType.valueOf(key);
        } catch (Exception e) {
            return null;
        }
    }
}

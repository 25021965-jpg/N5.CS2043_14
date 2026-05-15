package common;

public enum Command {
    LOGIN,
    REGISTER,
    LOGOUT,
    CREATE,
    JOIN,
    LIST,
    BID,
    GET_PROFILE,
    LIST_USERS,
    DELETE_USER,
    CANCEL_AUCTION;

    // parse an toàn (tránh crash)
    public static Command from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            // Loại bỏ khoảng trắng và viết hoa để khớp với Enum
            return Command.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Ghi log nhẹ ở đây để biết Client đang gửi bậy lệnh gì
            System.err.println("Unknown command: " + value);
            return null;
        }
    }
}
package common;

public enum Command {
    LOGIN,
    REGISTER,
    LOGOUT,
    LIST,
    JOIN,
    BID,
    CREATE,
    GET_PROFILE;

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

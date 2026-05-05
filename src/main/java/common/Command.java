package common;

public enum Command {
    LOGIN,
    REGISTER,
    LOGOUT,
    LIST,
    JOIN,
    BID,
    CREATE;

    // parse an toàn (tránh crash)
    public static Command from(String value) {
        try {
            return Command.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}

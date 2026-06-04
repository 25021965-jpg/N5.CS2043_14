package client.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class TextUtils {

    private static final String EMPTY_TEXT = "—";

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final NumberFormat USD_FORMAT =
            NumberFormat.getCurrencyInstance(
                    Locale.US
            );

    private TextUtils() {
    }

    // ==================== TITLE CASE ====================

    public static String toTitleCase(
            String input
    ) {

        if (input == null || input.isBlank()) {
            return EMPTY_TEXT;
        }

        return Arrays.stream(
                        input.trim()
                                .toLowerCase()
                                .replace("_", " ")
                                .split("\\s+")
                )

                .filter(word -> !word.isBlank())

                .map(word ->
                        Character.toUpperCase(word.charAt(0))
                                + word.substring(1)
                )

                .collect(Collectors.joining(" "));
    }

    // ==================== SAFE TEXT ====================

    public static String fallback(
            String value,
            String fallback
    ) {

        return (value == null || value.isBlank())
                ? fallback
                : value.trim();
    }

    public static String safeText(
            String value
    ) {

        return fallback(
                value,
                EMPTY_TEXT
        );
    }

    // ==================== CATEGORY ====================

    public static String formatCategory(
            String input
    ) {

        return "Category: "
                + toTitleCase(input);
    }

    // ==================== LABEL ====================

    public static String withLabel(
            String label,
            String value
    ) {

        return label
                + ": "
                + safeText(value);
    }

    // ==================== TRUNCATE ====================

    public static String truncate(
            String text,
            int maxLength
    ) {

        if (text == null || text.isBlank()) {
            return "";
        }

        text = text.trim();

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(
                0,
                maxLength
        ) + "...";
    }

    // ==================== USERNAME ====================

    public static String formatUsername(
            String username
    ) {

        if (username == null || username.isBlank()) {
            return "@unknown";
        }

        return username.startsWith("@")
                ? username
                : "@" + username;
    }

    // ==================== EMAIL ====================

    public static String hideEmail(
            String email
    ) {

        if (email == null || !email.contains("@")) {
            return EMPTY_TEXT;
        }

        String[] parts =
                email.split("@");

        if (parts.length != 2) {
            return EMPTY_TEXT;
        }

        String name =
                parts[0];

        if (name.length() <= 2) {
            return "***@" + parts[1];
        }

        return name.substring(0, 2)
                + "****@"
                + parts[1];
    }

    // ==================== ENUM ====================

    public static String formatEnum(
            String value
    ) {

        return toTitleCase(value);
    }

    // ==================== CAPITALIZE ====================
    public static String capitalizeFirst(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        text = text.trim();
        return Character.toUpperCase(text.charAt(0))
                + text.substring(1).toLowerCase();
    }

    // ==================== NORMALIZE ====================
    public static String normalizeSpaces(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
                .replaceAll("\\s+", " ");
    }

    // ==================== CURRENCY ====================
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return USD_FORMAT.format(amount);
    }

    // ==================== DATE TIME ====================
    public static String formatDateTime(LocalDateTime time) {
        if (time == null) {
            return EMPTY_TEXT;
        }

        return time.format(
                DATE_TIME_FORMATTER
        );
    }

    // ==================== REMAINING TIME ====================

    public static String formatRemainingTime(
            Duration duration
    ) {

        if (duration == null || duration.isNegative()) {
            return "Ended";
        }

        long hours =
                duration.toHours();

        long minutes =
                duration.toMinutesPart();

        long seconds =
                duration.toSecondsPart();

        return String.format(
                "%02dh %02dm %02ds",
                hours,
                minutes,
                seconds
        );
    }

    // ==================== BID INCREMENT ====================

    public static String formatBidIncrement(
            BigDecimal amount
    ) {

        return "+ "
                + formatCurrency(amount);
    }
}


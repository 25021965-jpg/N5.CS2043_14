package client.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TextUtils {

    // ===== Title Case =====
    // pending_approval -> Pending Approval
    // nguyen van a -> Nguyen Van A
    public static String toTitleCase(String input) {

        if (input == null || input.isBlank()) {
            return "—";
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

    // ===== Safe Text =====
    // null -> —
    public static String safeText(String input) {

        return (input == null || input.isBlank())
                ? "—"
                : input.trim();
    }

    // ===== Category Format =====
    // HOME_APPLIANCES -> Category: Home Appliances
    public static String formatCategory(String input) {

        return "Category: " + toTitleCase(input);
    }

    // ===== Label Prefix =====
    // Email: abc@gmail.com
    public static String withLabel(
            String label,
            String value
    ) {

        return label + ": " + safeText(value);
    }

    // ===== Truncate =====
    // Very long text -> Very long...
    public static String truncate(
            String text,
            int maxLength
    ) {

        if (text == null || text.isBlank()) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(
                0,
                maxLength
        ) + "...";
    }

    // ===== Username Format =====
    // nguyenvana -> @nguyenvana
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

    // ===== Hide Email =====
    // abcdef@gmail.com -> ab****@gmail.com
    public static String hideEmail(
            String email
    ) {

        if (email == null || !email.contains("@")) {
            return "—";
        }

        String[] parts =
                email.split("@");

        String name =
                parts[0];

        if (name.length() <= 2) {
            return "***@" + parts[1];
        }

        return name.substring(0, 2)
                + "****@"
                + parts[1];
    }

    // ===== Enum Format =====
    // ACTIVE -> Active
    // HOME_APPLIANCES -> Home Appliances
    public static String formatEnum(
            String value
    ) {

        return toTitleCase(value);
    }

    // ===== Empty Fallback =====
    // "" -> No Data
    public static String fallback(
            String value,
            String fallback
    ) {

        return (value == null || value.isBlank())
                ? fallback
                : value;
    }

    // ===== First Letter Only =====
    // nguyen -> Nguyen
    public static String capitalizeFirst(
            String text
    ) {

        if (text == null || text.isBlank()) {
            return "";
        }

        text = text.trim();

        return Character.toUpperCase(text.charAt(0))
                + text.substring(1).toLowerCase();
    }

    // ===== Remove Extra Spaces =====
    // "  nguyen    van   a  " -> "nguyen van a"
    public static String normalizeSpaces(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return text.trim()
                .replaceAll("\\s+", " ");
    }
}
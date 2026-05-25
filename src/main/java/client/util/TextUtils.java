package client.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TextUtils {

    // ===== Title Case: pending_approval → Pending Approval =====
    public static String toTitleCase(String input) {
        if (input == null || input.isBlank()) return "—";

        String lower = input.toLowerCase().replace("_", " ");

        StringBuilder sb = new StringBuilder();

        for (String word : lower.split(" ")) {
            if (word.isEmpty()) continue;

            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }

        return sb.toString().trim();
    }


    // ===== Category format: HOME_APPLIANCES → Home Appliances =====
    public static String formatCategory(String input) {
        return "Category: " + toTitleCase(input);
    }


    // ===== Label prefix helper =====
    public static String withLabel(String label, String value) {
        return label + ": " + (value == null ? "—" : value);
    }
}
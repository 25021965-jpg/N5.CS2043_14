package client.network.response.parser;

import java.util.ArrayList;
import java.util.List;

public class ItemParser {

    public static List<String[]> parse(String data, int minFields) {

        List<String[]> items = new ArrayList<>();
        if (data == null || data.isBlank()) return items;

        for (String token : data.split("\\|")) {
            String[] fields = token.split(";", -1);
            if (fields.length >= minFields) {
                items.add(fields);
            }
        }

        return items;
    }
}
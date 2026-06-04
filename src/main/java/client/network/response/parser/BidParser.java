package client.network.response.parser;

import model.Bid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BidParser {

    public static List<Bid> parse(String data) {

        List<Bid> history = new ArrayList<>();
        if (data == null || data.isBlank()) return history;

        for (String token : data.split("\\|")) {
            try {
                String[] parts = token.split(";", -1);

                if (parts.length < 3) continue;

                Bid bid = new Bid();
                bid.setUsername(parts[0]);
                bid.setAmount(new BigDecimal(parts[1]));
                bid.setTimeString(parts[2]);

                history.add(bid);

            } catch (Exception ignored) {
                // bỏ record lỗi, không crash UI
            }
        }

        return history;
    }
}
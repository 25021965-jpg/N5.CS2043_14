package client.network.response.parser;

import model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class AuctionParser {

    public static List<Auction> parseList(String data) {

        List<Auction> list = new ArrayList<>();
        if (data == null || data.isBlank()) return list;

        for (String token : data.split("\\|")) {
            try {
                String[] p = token.split(";", -1);
                if (p.length < 12) continue;

                Auction a = new Auction();
                a.setAuction_id(p[0]);

                Item item = new Item();
                item.setItem_id(p[1]);
                item.setName(p[2]);
                item.setDescription(p[9]);

                try {
                    item.setCategory(Category.valueOf(p[8].toUpperCase()));
                } catch (Exception e) {
                    item.setCategory(Category.OTHER);
                }

                if (!p[5].isBlank()) {
                    item.setImages(List.of(p[5]));
                }

                a.setItem(item);
                a.setCurrentPrice(new BigDecimal(p[3]));
                a.setMinIncrement(new BigDecimal(p[4]));
                a.setStartTime(LocalDateTime.parse(p[6]));
                a.setEndTime(LocalDateTime.parse(p[7]));
                a.setStatus(AuctionStatus.valueOf(p[10].toUpperCase()));

                list.add(a);

            } catch (Exception ignored) {}
        }

        return list;
    }
}
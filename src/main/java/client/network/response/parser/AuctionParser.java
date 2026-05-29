package client.network.response.parser;

import model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class AuctionParser {

    public static List<Auction> parseList(String data) {

        List<Auction> list = new ArrayList<>();

        if (data == null || data.isBlank()) {
            return list;
        }

        for (String token : data.split("\\|")) {

            try {

                String[] p = token.split(";", -1);

                if (p.length < 12) {
                    continue;
                }

                Auction auction = new Auction();

                auction.setAuction_id(p[0]);

                Item item = new Item();

                item.setItem_id(p[1]);
                item.setName(p[2]);
                item.setDescription(p[9]);

                try {
                    item.setCategory(
                            Category.valueOf(
                                    p[8].trim().toUpperCase()
                            )
                    );
                } catch (Exception e) {
                    item.setCategory(Category.OTHER);
                }

                // ================= IMAGES =================

                List<String> images = new ArrayList<>();

                if (!p[5].isBlank()
                        && !p[5].equalsIgnoreCase("NO_IMAGE")) {

                    for (String image : p[5].split(",")) {

                        String trimmed = image.trim();

                        if (!trimmed.isEmpty()) {
                            images.add(trimmed);
                        }
                    }
                }

                item.setImages(images);

                auction.setItem(item);

                // ================= PRICE =================

                auction.setCurrentPrice(
                        new BigDecimal(p[3])
                );

                auction.setMinIncrement(
                        new BigDecimal(p[4])
                );

                // ================= TIME =================

                auction.setStartTime(
                        LocalDateTime.parse(p[6])
                );

                auction.setEndTime(
                        LocalDateTime.parse(p[7])
                );

                // ================= STATUS =================

                String statusRaw =
                        p[10]
                                .trim()
                                .toUpperCase(Locale.ROOT);

                try {

                    auction.setStatus(
                            AuctionStatus.valueOf(statusRaw)
                    );

                } catch (Exception e) {

                    System.out.println(
                            "BAD STATUS: " + statusRaw
                    );

                    auction.setStatus(
                            AuctionStatus.PENDING_APPROVAL
                    );
                }

                // ================= SELLER =================

                User seller = new User();

                seller.setUser_id(p[11]);

                auction.setSeller(seller);

                list.add(auction);

            } catch (Exception e) {

                System.out.println(
                        "PARSE ERROR: " + token
                );

                e.printStackTrace();
            }
        }

        return list;
    }
}
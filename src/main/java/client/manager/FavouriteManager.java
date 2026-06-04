package client.manager;

import client.network.ClientSocket;

import java.util.HashSet;
import java.util.Set;

public class FavouriteManager {
    private static final Set<String> favouriteItemIds = new HashSet<>();

    public static boolean isFavourite(String itemId) {
        return favouriteItemIds.contains(itemId);
    }

    public static void addFavourite(String itemId) {
        favouriteItemIds.add(itemId);
    }

    public static void removeFavourite(String itemId) {
        favouriteItemIds.remove(itemId);
    }

    public static Set<String> getFavouriteItemIds() {
        return favouriteItemIds;
    }

    public static void loadFavourite(ClientSocket client) {
        if (client != null) {
            client.sendMessage("LIST_FAVOURITES");
        }
    }

    public static void loadFromResponse(String response) {
        favouriteItemIds.clear();

        if (response == null || response.isBlank()) {
            return;
        }

        String[] auctions = response.split("\\|");

        for (String auction : auctions) {
            String[] p = auction.split(";", -1);

            if (p.length < 2) {
                continue;
            }

            favouriteItemIds.add(p[1]);
        }
    }

    public static void clear() {
        favouriteItemIds.clear();
    }
}
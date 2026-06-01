package client.manager;

import java.util.HashSet;
import java.util.Set;

public class FavouriteManager {

    // LƯU ITEM ID
    public static Set<String> favouriteItemIds = new HashSet<>();
    public static boolean isFavourite(String itemId) {
        return favouriteItemIds.contains(itemId);
    }

    public static void addFavourite(String itemId) {
        favouriteItemIds.add(itemId);
    }
    public static void removeFavourite(String itemId) {
        favouriteItemIds.remove(itemId);
    }
    public static Set<String> getFavouriteItemIds() {return favouriteItemIds;}

    public static void loadFromResponse(String response) {
        favouriteItemIds.clear();
        if(response == null || response.isBlank()) {
            return;
        }
        String[] auctions = response.split("\\|");
        for(String auction : auctions) {
            String[] p = auction.split(";", -1);
            if(p.length < 2) continue;
            String itemId = p[1];
            favouriteItemIds.add(itemId);
        }
    }

    public static void clear() {
        favouriteItemIds.clear();
    }
}
package client.util;

import client.manager.FavouriteManager;
import model.Auction;
import model.Entity.Item.Category;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class AuctionListHelper {

    private AuctionListHelper() {
    }

    public static boolean isSameData(
            List<Auction> oldList,
            List<Auction> newList
    ) {
        if (oldList.size() != newList.size()) {
            return false;
        }

        for (int i = 0; i < newList.size(); i++) {
            Auction oldAuction = oldList.get(i);
            Auction newAuction = newList.get(i);
            if (!Objects.equals(oldAuction.getAuction_id(), newAuction.getAuction_id())) {
                return false;
            }
            if (!Objects.equals(oldAuction.getCurrentPrice(), newAuction.getCurrentPrice())) {
                return false;
            }
            if (oldAuction.getStatus() != newAuction.getStatus()) {
                return false;
            }
        }
        return true;
    }

    public static List<Auction> filterAuctions(
            List<Auction> auctions,
            String keyword,
            Category selectedCategory,
            String selectedStatus
    ) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();

        return auctions.stream()
                .filter(auction -> matchesSearch(auction, normalizedKeyword))
                .filter(auction -> matchesCategory(auction, selectedCategory))
                .filter(auction -> matchesStatus(auction, selectedStatus))
                .sorted(AuctionListHelper::compareAuctions)
                .collect(Collectors.toList());
    }

    public static boolean matchesSearch(
            Auction auction,
            String keyword
    ) {
        if (auction == null
                || auction.getItem() == null
                || auction.getItem().getName() == null) {
            return false;
        }
        return keyword.isEmpty()
                || auction.getItem().getName().toLowerCase().contains(keyword);
    }

    public static int compareAuctions(Auction a1, Auction a2) {
        boolean fav1 = FavouriteManager.isFavourite(a1.getItem().getItem_id());
        boolean fav2 = FavouriteManager.isFavourite(a2.getItem().getItem_id());

        if (fav1 != fav2) {
            return fav1 ? -1 : 1;
        }

        int priority1 = getPriority(a1);
        int priority2 = getPriority(a2);
        return Integer.compare(priority1, priority2);
    }

    private static int getPriority(Auction auction) {
        return switch (auction.getStatus()) {
            case ACTIVE -> 0;
            case UPCOMING -> 1;
            default -> 2;
        };
    }

    public static boolean matchesCategory(
            Auction auction,
            Category selectedCategory
    ) {
        return selectedCategory == null
                || auction.getItem() != null
                && auction.getItem().getCategory() == selectedCategory;
    }

    public static boolean matchesStatus(
            Auction auction,
            String selectedStatus
    ) {
        return selectedStatus == null
                || auction.getStatus() != null
                && auction.getStatus().name().equalsIgnoreCase(selectedStatus);
    }
}

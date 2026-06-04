package server.network.handler;

import model.*;
import model.Entity.Item.Item;
import model.Entity.User.User;
import server.dao.FavouriteDAO;
import server.service.AuctionService;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavouriteHandler extends BaseHandler {

    public FavouriteHandler(User currentUser,
                            PrintWriter writer,
                            String currentAuctionId) {

        super(currentUser, writer, currentAuctionId);
    }

    // ==================== ADD FAVOURITE ====================
    public String handleAddFavourite(String[] data) {
        if (currentUser == null)
            return "ADD_FAVOURITE_FAILED|Not logged in";

        if (data.length < 2)
            return "ADD_FAVOURITE_FAILED|Missing item id";

        String itemId = data[1];

        boolean ok =
                FavouriteDAO.addFavourite(
                        currentUser.getUser_id(),
                        itemId
                );
        System.out.println("ADD_FAVOURITE DAO RESULT = " + ok);
        return ok
                ? "ADD_FAVOURITE_SUCCESS"
                : "ADD_FAVOURITE_FAILED";
    }

    // ==================== REMOVE FAVOURITE ====================

    public String handleRemoveFavourite(String[] data) {

        if (currentUser == null)
            return "REMOVE_FAVOURITE_FAILED|Not logged in";

        if (data.length < 2)
            return "REMOVE_FAVOURITE_FAILED|Missing item id";

        String itemId = data[1];

        boolean ok =
                FavouriteDAO.removeFavourite(
                        currentUser.getUser_id(),
                        itemId
                );

        return ok
                ? "REMOVE_FAVOURITE_SUCCESS"
                : "REMOVE_FAVOURITE_FAILED";
    }

    // ==================== LIST FAVOURITES ====================

    public String handleListFavourites() {
        if (currentUser == null)
            return "FAVOURITES_FAILED|Not logged in";

        Set<String> favouriteItemIds =
                new HashSet<>(
                        FavouriteDAO.getFavouriteItemIds(
                                currentUser.getUser_id()
                        )
                );

        if (favouriteItemIds.isEmpty())
            return "LIST_FAVOURITES_EMPTY";

        List<Auction> auctions =
                AuctionService.getAllAuctions();

        StringBuilder sb = new StringBuilder();

        int count = 0;

        for (Auction auction : auctions) {

            if (auction == null
                    || auction.getItem() == null)
                continue;

            Item item = auction.getItem();

            String itemId = item.getItem_id();

            if (!favouriteItemIds.contains(itemId))
                continue;

            String allImgs = "NO_IMAGE";

            if (item.getImages() != null
                    && !item.getImages().isEmpty()) {

                allImgs =
                        String.join(",", item.getImages());
            }

            String itemName =
                    item.getName() == null
                            ? "Unnamed"
                            : item.getName()
                              .replace(";", ",")
                              .replace("|", "-");

            String description =
                    item.getDescription() == null
                            ? ""
                            : item.getDescription()
                              .replace(";", ",")
                              .replace("|", "-")
                              .replace("\n", " ");

            String sellerId =
                    (auction.getSeller() != null
                            && auction.getSeller().getUser_id() != null)
                            ? auction.getSeller().getUser_id()
                            : "";

            if (count == 0) {
                sb.append("LIST_FAVOURITES_SUCCESS");
            }

            sb.append("|")
                    .append(auction.getAuction_id()).append(";")
                    .append(item.getItem_id()).append(";")
                    .append(itemName).append(";")
                    .append(auction.getCurrentPrice()).append(";")
                    .append(auction.getMinIncrement()).append(";")
                    .append(allImgs).append(";")
                    .append(auction.getStartTime()).append(";")
                    .append(auction.getEndTime()).append(";")
                    .append(item.getCategory()).append(";")
                    .append(description).append(";")
                    .append(auction.getStatus()).append(";")
                    .append(sellerId);

            count++;
        }

        return count == 0
                ? "LIST_FAVOURITES_EMPTY"
                : sb.toString();
    }
}
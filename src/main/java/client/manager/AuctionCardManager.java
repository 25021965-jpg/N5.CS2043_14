package client.manager;

import client.controller.ItemCardController;
import client.network.ClientSocket;
import client.util.AuctionCardFactory;
import javafx.scene.Parent;
import model.Auction;

import java.util.HashMap;
import java.util.Map;

public class AuctionCardManager {

    // ==================== CACHE (NODE + CONTROLLER SAFE) ====================
    private static final Map<String, Parent> CARD_CACHE = new HashMap<>();
    private static final Map<String, ItemCardController> CONTROLLER_CACHE = new HashMap<>();

    // ==================== GET OR CREATE ====================
    public static Parent getCard(Auction auction, ClientSocket client) {

        if (auction == null || auction.getAuction_id() == null) {
            return null;
        }

        String id = auction.getAuction_id();

        // ==================== EXISTING CARD ====================
        if (CARD_CACHE.containsKey(id)) {

            Parent cachedNode = CARD_CACHE.get(id);
            ItemCardController controller = CONTROLLER_CACHE.get(id);

            if (controller != null) {
                controller.updateAuction(auction);
            } else {
                // fallback safety (recreate if broken cache)
                Parent newCard = AuctionCardFactory.createCard(auction, client);
                CARD_CACHE.put(id, newCard);
                CONTROLLER_CACHE.put(id,
                        (ItemCardController) newCard.getProperties().get("controller"));
                return newCard;
            }

            return cachedNode;
        }

        // ==================== CREATE NEW CARD ====================
        Parent newCard = AuctionCardFactory.createCard(auction, client);

        if (newCard == null) {
            return null;
        }

        ItemCardController controller =
                (ItemCardController) newCard.getProperties().get("controller");

        if (controller == null) {
            System.err.println("Missing controller for auction: " + id);
            return newCard;
        }

        CARD_CACHE.put(id, newCard);
        CONTROLLER_CACHE.put(id, controller);

        return newCard;
    }

    // ==================== REMOVE CARD ====================
    public static void removeCard(String auctionId) {
        CARD_CACHE.remove(auctionId);
        CONTROLLER_CACHE.remove(auctionId);
    }

    // ==================== CLEAR ALL ====================
    public static void clear() {
        CARD_CACHE.clear();
        CONTROLLER_CACHE.clear();
    }
}
package client.util;

import client.controller.ItemCardController;
import client.network.ClientSocket;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import model.Auction;

public class AuctionCardFactory {

    public static Parent createCard(Auction auction, ClientSocket client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AuctionCardFactory.class.getResource("/fxml/itemsCard-view.fxml")
            );

            Parent root = loader.load();
            ItemCardController controller = loader.getController();

            // IMPORTANT: bind controller + id
            root.getProperties().put("controller", controller);
            root.setUserData(auction.getAuction_id());

            controller.setClient(client);
            controller.setData(auction);

            return root;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
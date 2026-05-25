package client.network.response.handler;

import client.controller.HomePageController;
import client.controller.ManageProductController;
import client.controller.LiveAuctionController;
import client.controller.MyAuctionsController;
import client.network.ClientSocket;
import client.network.response.parser.AuctionParser;
import client.network.response.parser.BidParser;
import client.network.response.parser.ItemParser;
import client.util.NavigationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import model.Bid;

public class AuctionHandler {

    // ================= AUCTION LIST =================
    public static void list(String data) {
        HomePageController ctrl = HomePageController.getInstance();
        if (ctrl != null) {
            ctrl.updateAuctionList(AuctionParser.parseList(data));
        }
    }

    public static void listEmpty() {
        HomePageController ctrl = HomePageController.getInstance();
        if (ctrl != null) {
            ctrl.updateAuctionList(java.util.List.of());
        }
    }

    // ================= CREATE AUCTION =================
    public static void createSuccess(Stage stage) {
        NavigationUtils.showToast(stage, "Auction submitted!");
        NavigationUtils.switchScene(stage, "/fxml/HomePage.fxml", "Home");
    }
    public static void createFailed(String data, Stage stage) {
        NavigationUtils.showError("Failed to create auction: " + data);
    }

    // ================= MY AUCTIONS =================
    public static void myAuctions(String data) {

        MyAuctionsController ctrl = MyAuctionsController.getInstance();

        if (ctrl != null) {
            ctrl.updateMyAuctions(
                    AuctionParser.parseList(data)
            );
        }
    }

    // ================= BID =================
    public static void bidSuccess(Stage stage) {
        NavigationUtils.showToast(stage, "Bid placed!");
        ClientSocket.getInstance().sendList();
    }

    public static void bidFailed(String data, Stage stage) {
        NavigationUtils.showError("Bid failed: " + data);
    }

    public static void bidHistory(String data) {
        ObservableList<Bid> bids =
                FXCollections.observableArrayList(BidParser.parse(data));

        LiveAuctionController ctrl = LiveAuctionController.getInstance();
        if (ctrl != null) {
            ctrl.loadBidHistory(bids);
        }
    }

    public static void updatePrice(String raw) {

        if (LiveAuctionController.getInstance() != null) {
            LiveAuctionController.getInstance().handleLiveUpdate(raw);
        }
    }

    // ================= JOIN AUCTION =================
    public static void joinSuccess(String data, Stage stage) {
        NavigationUtils.switchScene(stage, "/fxml/auctionRoom-view.fxml", "Auction Room");
    }

    public static void joinFailed(String data, Stage stage) {
        NavigationUtils.showError("Join failed: " + data);
    }

    // ================= ITEMS / PRODUCTS =================
    public static void items(String data) {

        ManageProductController ctrl = ManageProductController.getInstance();
        if (ctrl != null) {

            ObservableList<String[]> items =
                    FXCollections.observableArrayList(
                            ItemParser.parse(data, 5)
                    );

            ctrl.updateProducts(items);
        }
    }

    public static void itemsEmpty() {

        ManageProductController ctrl = ManageProductController.getInstance();
        if (ctrl != null) {
            ctrl.updateProducts(FXCollections.observableArrayList());
        }
    }

    // ================= DELETE ITEM =================
    public static void deleteSuccess(Stage stage) {

        NavigationUtils.showToast(stage, "Deleted!");

        ManageProductController ctrl = ManageProductController.getInstance();
        if (ctrl != null) {
            ctrl.handleReloadProducts();
        }
    }

    public static void deleteFailed(String data, Stage stage) {
        NavigationUtils.showError("Delete failed: " + data);
    }

    // ================= UPDATE ITEM =================
    public static void updateSuccess(Stage stage) {

        NavigationUtils.showToast(stage, "Updated!");

        ManageProductController ctrl = ManageProductController.getInstance();
        if (ctrl != null) {
            ctrl.handleReloadProducts();
        }
    }

    public static void updateFailed(String data, Stage stage) {
        NavigationUtils.showError("Update failed: " + data);
    }
}
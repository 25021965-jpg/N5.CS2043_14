package client.network.response.handler;

import client.controller.*;
import client.controller.admin.ManageProductController;

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

    // ===== AUCTION LIST =====

    public static void list(String data) {

        System.out.println("RAW LIST RESPONSE: " + data);

        HomePageController ctrl =
                HomePageController.getInstance();

        if (ctrl != null) {

            ctrl.updateAuctionList(
                    AuctionParser.parseList(data)
            );
        }
    }

    public static void listEmpty() {

        HomePageController ctrl =
                HomePageController.getInstance();

        if (ctrl != null) {

            ctrl.updateAuctionList(
                    java.util.List.of()
            );
        }
    }

    // ===== CREATE AUCTION =====

    public static void createSuccess(Stage stage) {

        NavigationUtils.showToast(
                stage,
                "Auction submitted!"
        );

        NavigationUtils.switchScene(
                stage,
                "/fxml/HomePage.fxml",
                "Home"
        );
    }

    public static void createFailed(String data) {
        NavigationUtils.showError(
                "Failed to create auction: " + data
        );
    }

    // ===== JOINED AUCTIONS =====

    public static void joinedAuctions(String data) {

        AuctionHistoryController ctrl =
                AuctionHistoryController.getInstance();

        if (ctrl != null) {

            ctrl.renderHistory(
                    "LIST_JOINED_AUCTIONS_SUCCESS|" + data
            );
        }
    }

    public static void joinedAuctionsEmpty() {

        AuctionHistoryController ctrl =
                AuctionHistoryController.getInstance();

        if (ctrl != null) {

            ctrl.renderHistory(
                    "LIST_JOINED_AUCTIONS_EMPTY"
            );
        }
    }

    // ===== MY AUCTIONS =====

    public static void myAuctions(String data) {

        System.out.println("RAW MY AUCTIONS: " + data);

        MyAuctionsController ctrl =
                MyAuctionsController.getInstance();

        if (ctrl != null) {

            ctrl.updateMyAuctions(
                    AuctionParser.parseList(data)
            );
        }
    }

    public static void myAuctionsEmpty() {

        MyAuctionsController ctrl =
                MyAuctionsController.getInstance();

        if (ctrl != null) {

            ctrl.updateMyAuctions(
                    java.util.List.of()
            );
        }
    }

    // ===== BID =====

    public static void bidSuccess(Stage stage) {

        NavigationUtils.showToast(
                stage,
                "Bid placed!"
        );

        ClientSocket
                .getInstance()
                .sendList();
    }

    public static void bidFailed(
            String data
    ) {

        NavigationUtils.showError(
                "Bid failed: " + data
        );
    }

    // ===== BID HISTORY =====

    public static void bidHistory(String data) {

        ObservableList<Bid> bids =
                FXCollections.observableArrayList(
                        BidParser.parse(data)
                );

        LiveAuctionController ctrl =
                LiveAuctionController.getInstance();

        if (ctrl != null) {

            ctrl.loadBidHistory(bids);
        }
    }

    public static void bidHistoryEmpty() {

        LiveAuctionController ctrl =
                LiveAuctionController.getInstance();

        if (ctrl != null) {

            ctrl.loadBidHistory(
                    FXCollections.observableArrayList()
            );
        }
    }

    // ===== LIVE UPDATE =====

    public static void updatePrice(String raw) {

        LiveAuctionController ctrl =
                LiveAuctionController.getInstance();

        if (ctrl != null) {

            ctrl.handleServerMessage(raw);
        }
    }

    // ===== JOIN AUCTION =====

    public static void joinSuccess(
            String data
    ) {

        LiveAuctionController ctrl =
                LiveAuctionController.getInstance();

        if (ctrl != null) {

            ctrl.handleServerMessage(
                    "JOIN_SUCCESS|" + data
            );
        }
    }

    public static void joinFailed(
            String data
    ) {

        NavigationUtils.showError(
                "Join failed: " + data
        );
    }

    // ===== ITEMS =====

    public static void items(String data) {

        ManageProductController ctrl =
                ManageProductController.getInstance();

        if (ctrl != null) {

            ObservableList<String[]> items =
                    FXCollections.observableArrayList(
                            ItemParser.parse(data, 5)
                    );

            ctrl.updateProducts(items);
        }
    }

    public static void itemsEmpty() {

        ManageProductController ctrl =
                ManageProductController.getInstance();

        if (ctrl != null) {

            ctrl.updateProducts(
                    FXCollections.observableArrayList()
            );
        }
    }

    // ===== DELETE ITEM =====

    public static void deleteSuccess(Stage stage) {

        NavigationUtils.showToast(
                stage,
                "Deleted!"
        );

        ManageProductController ctrl =
                ManageProductController.getInstance();

        if (ctrl != null) {

            ctrl.handleReloadProducts();
        }
    }

    public static void deleteFailed(
            String data
    ) {

        NavigationUtils.showError(
                "Delete failed: " + data
        );
    }

    // ===== UPDATE ITEM =====

    public static void updateSuccess(Stage stage) {

        NavigationUtils.showToast(
                stage,
                "Updated!"
        );

        ManageProductController ctrl =
                ManageProductController.getInstance();

        if (ctrl != null) {

            ctrl.handleReloadProducts();
        }
    }

    public static void updateFailed(String data) {
        NavigationUtils.showError(
                "Update failed: " + data
        );
    }
}
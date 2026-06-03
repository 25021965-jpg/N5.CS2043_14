package client.network.response.handler;

import client.controller.*;
import client.controller.AdminManageProductController;

import client.manager.ControllerRegistry;
import client.network.ClientSocket;
import client.network.response.parser.AuctionParser;
import client.network.response.parser.BidParser;
import client.network.response.parser.ItemParser;

import client.util.AlertUtils;
import client.util.NavigationUtils;

import client.util.ToastUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.stage.Stage;

import model.Bid;

public class AuctionHandler {

    // ===== AUCTION LIST =====
    public static void list(String data) {
        UserHomePageController ctrl = ControllerRegistry.get(UserHomePageController.class);
        if (ctrl != null) {
            ctrl.updateAuctionList(
                    AuctionParser.parseList(data)
            );
        }
    }

    public static void listEmpty() {
        UserHomePageController ctrl = ControllerRegistry.get(UserHomePageController.class);
        if (ctrl != null) {
            ctrl.updateAuctionList(
                    java.util.List.of()
            );
        }
    }

    // ===== CREATE AUCTION =====

    public static void createSuccess(Stage stage) {
        ToastUtils.show(stage, "Auction submitted!");
        NavigationUtils.switchScene(
                stage,
                "/fxml/userHomePage-view.fxml",
                "Home"
        );
    }

    public static void createFailed(String data) {
        AlertUtils.error("Failed to create auction: " + data);
    }

    // ===== JOINED AUCTIONS =====
    public static void joinedAuctions(String data) {
        UserAuctionHistoryController ctrl = ControllerRegistry.get(UserAuctionHistoryController.class);

        if (ctrl != null) {
            ctrl.renderHistory(
                    "LIST_JOINED_AUCTIONS_SUCCESS|" + data
            );
        }
    }

    public static void joinedAuctionsEmpty() {
        UserAuctionHistoryController ctrl = ControllerRegistry.get(UserAuctionHistoryController.class);
        if (ctrl != null) {

            ctrl.renderHistory(
                    "LIST_JOINED_AUCTIONS_EMPTY"
            );
        }
    }

    // ===== CREATED AUCTIONS =====

    public static void CreatedAuctions(String data) {
        System.out.println("RAW CREATED AUCTIONS: " + data);
        UserCreatedAuctionsController ctrl = ControllerRegistry.get(UserCreatedAuctionsController.class);
        if (ctrl != null) {
            ctrl.updateCreatedAuctions(AuctionParser.parseList(data));
        }
    }

    public static void CreatedAuctionsEmpty() {
        UserCreatedAuctionsController ctrl = ControllerRegistry.get(UserCreatedAuctionsController.class);
        if (ctrl != null) {
            ctrl.updateCreatedAuctions(java.util.List.of());
        }
    }

    // ===== BID =====
    public static void bidSuccess(Stage stage) {
        ToastUtils.show(stage, "Bid placed!");

        ClientSocket
                .getInstance()
                .sendList();
    }

    public static void bidFailed(String data) {
        AlertUtils.error("Bid failed: " + data);
    }

    // ===== BID HISTORY =====
    public static void bidHistory(String data) {
        ObservableList<Bid> bids =
                FXCollections.observableArrayList(
                        BidParser.parse(data)
                );

        UserLiveAuctionController ctrl = UserLiveAuctionController.getInstance();
        if (ctrl != null) {
            ctrl.loadBidHistory(bids);
        }
    }

    public static void bidHistoryEmpty() {

        UserLiveAuctionController ctrl =
                UserLiveAuctionController.getInstance();

        if (ctrl != null) {

            ctrl.loadBidHistory(
                    FXCollections.observableArrayList()
            );
        }
    }

    // ===== LIVE UPDATE =====

    public static void updatePrice(String raw) {

        UserLiveAuctionController ctrl =
                UserLiveAuctionController.getInstance();

        if (ctrl != null) {

            ctrl.handleServerMessage(raw);
        }
    }

    // ===== JOIN AUCTION =====

    public static void joinSuccess(
            String data
    ) {

        UserLiveAuctionController ctrl =
                UserLiveAuctionController.getInstance();

        if (ctrl != null) {

            ctrl.handleServerMessage(
                    "JOIN_SUCCESS|" + data
            );
        }
    }

    public static void joinFailed(
            String data
    ) {

        AlertUtils.error(
                "Join failed: " + data
        );
    }

    // ===== ITEMS =====

    public static void items(String data) {

        AdminManageProductController ctrl =
                AdminManageProductController.getInstance();

        if (ctrl != null) {

            ObservableList<String[]> items =
                    FXCollections.observableArrayList(
                            ItemParser.parse(data, 5)
                    );

            ctrl.updateProducts(items);
        }
    }

    public static void itemsEmpty() {

        AdminManageProductController ctrl =
                AdminManageProductController.getInstance();

        if (ctrl != null) {

            ctrl.updateProducts(
                    FXCollections.observableArrayList()
            );
        }
    }

    // ===== DELETE ITEM =====

    public static void deleteSuccess(Stage stage) {

        ToastUtils.show(
                stage,
                "Deleted!"
        );

        AdminManageProductController ctrl =
                AdminManageProductController.getInstance();

        if (ctrl != null) {

            ctrl.handleReloadProducts();
        }
    }

    public static void deleteFailed(
            String data
    ) {

        AlertUtils.error(
                "Delete failed: " + data
        );
    }

    // ===== UPDATE ITEM =====

    public static void updateSuccess(Stage stage) {
        ToastUtils.show(
                stage,
                "Updated!"
        );

        AdminManageProductController ctrl =
                AdminManageProductController.getInstance();

        if (ctrl != null) {
            ctrl.handleReloadProducts();
        }
    }

    public static void updateFailed(String data) {
        AlertUtils.error("Update failed: " + data);
    }
}
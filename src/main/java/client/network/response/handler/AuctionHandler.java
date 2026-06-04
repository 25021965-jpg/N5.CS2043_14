package client.network.response.handler;

import client.controller.*;
import client.controller.AdminManageProductController;

import client.manager.AuctionHistoryManager;
import client.manager.AuctionStateManager;
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
import model.Auction;
import client.manager.UserSession;
import model.ParticipationStatus;

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

        var auctions = AuctionParser.parseList(data);

        AuctionHistoryManager.addAuctions(auctions);

        for (var auction : auctions) {

            ParticipationStatus current =
                    AuctionStateManager.getParticipation(
                            auction.getAuction_id()
                    );

            if (current == ParticipationStatus.NOT_JOINED
                    || current == ParticipationStatus.LEFT) {

                AuctionStateManager.setParticipation(
                        auction.getAuction_id(),
                        ParticipationStatus.JOINED
                );
            }
        }

        UserAuctionHistoryController ctrl =
                ControllerRegistry.get(UserAuctionHistoryController.class);

        if (ctrl != null) {
            ctrl.renderHistory(
                    "LIST_JOINED_AUCTIONS_SUCCESS|" + data
            );
        }
    }

    public static void joinedAuctionsEmpty() {
        UserAuctionHistoryController ctrl =
                ControllerRegistry.get(UserAuctionHistoryController.class);

        if (ctrl != null) {
            ctrl.renderHistory("LIST_JOINED_AUCTIONS_EMPTY");
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
            ctrl.loadBidHistory(FXCollections.observableArrayList());
        }
    }

    // ===== LIVE UPDATE =====
    public static void updatePrice(String raw) {
        UserLiveAuctionController ctrl =
                UserLiveAuctionController.getInstance();
        if (ctrl != null) {
            ctrl.handleServerMessage(raw);
        }

        // Also update homepage card if present
        try {
            String[] parts = raw.split("\\|");
            if (parts.length >= 3) {
                String auctionId = parts[1];
                String newPrice = parts[2];
                UserHomePageController home = ControllerRegistry.get(UserHomePageController.class);
                if (home != null) {
                    home.updateCardPrice(auctionId, newPrice);
                }
            }
        } catch (Exception ignored) {}
    }

    // ===== JOIN AUCTION =====
    public static void joinSuccess(String data) {
        String[] parts = data.split("\\|");
        if (parts.length > 0) {
            String auctionId = parts[0];
            ParticipationStatus current =
                    AuctionStateManager.getParticipation(auctionId);
            System.out.println(
                    "JOIN_SUCCESS current status = "
                            + AuctionStateManager.getParticipation(auctionId)
            );

            // Trong đoạn else của JOIN_SUCCESS, thêm check:
            if (current != ParticipationStatus.NOT_JOINED
                    && current != ParticipationStatus.WON
                    && current != ParticipationStatus.LOST
                    && current != ParticipationStatus.LEADING) { // ← thêm dòng này

                AuctionStateManager.setParticipation(auctionId, ParticipationStatus.OUTBID);
            }
        }

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

    public static void auctionDetail(String data) {
        // data: single token with fields matching AuctionParser.parseList format
        try {
            var list = AuctionParser.parseList(data);
            if (list == null || list.isEmpty()) return;

            Auction auction = list.get(0);

            // open item view on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                try {
                    Stage stage = NavigationUtils.getMainStage();
                    if (stage == null) stage = NavigationUtils.getCurrentStage();

                    client.controller.ItemViewController ctrl = NavigationUtils.switchSceneAndGetController(
                            stage,
                            "/fxml/item-view.fxml",
                            "Item Detail"
                    );

                    ctrl.setClient(ClientSocket.getInstance());
                    ctrl.setUser(UserSession.getCurrentUser());
                    ctrl.setAuctionData(auction);

                } catch (Exception e) {
                    System.err.println("Failed to open auction detail: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("Failed to parse auction detail: " + e.getMessage());
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

    public static void deleteFailed(String data) {
        AlertUtils.error("Delete failed: " + data);
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

    public static void auctionStates(String data) {
        String[] auctions = data.split("\\|");

        for (int i = 0; i < auctions.length; i++) {
            String[] parts = auctions[i].split(";");

            if (parts.length < 2) continue;

            String auctionId = parts[0];
            ParticipationStatus status = ParticipationStatus.valueOf(parts[1]);

            // THÊM: không override WON/LOST
            ParticipationStatus current = AuctionStateManager.getParticipation(auctionId);
            if (current == ParticipationStatus.WON
                    || current == ParticipationStatus.LOST) {
                continue;
            }

            AuctionStateManager.setParticipation(auctionId, status);
        }
    }
}
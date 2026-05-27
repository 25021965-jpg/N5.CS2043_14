package client.network.response;

import client.network.response.handler.*;
import client.util.NavigationUtils;
import common.ResponseType;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ResponseRouter {

    public static void route(String raw, Stage stage) {

        // LIVE UPDATE
        if (raw.startsWith("UPDATE_PRICE")) {
            Platform.runLater(() -> AuctionHandler.updatePrice(raw));
            return;
        }


        String[] parts = raw.split("\\|", 2);

        String header = parts[0];

        String data =
                parts.length > 1
                        ? parts[1]
                        : "";

        ResponseType type;

        try {

            type = ResponseType.from(header);

            if (type == null) return;

        } catch (Exception e) {
            return;
        }

        Platform.runLater(
                () -> dispatch(type, data, stage)
        );
    }

    private static void dispatch(
            ResponseType type,
            String data,
            Stage stage
    ) {

        switch (type) {

            // ===== AUTH =====

            case LOGIN_SUCCESS ->
                    AuthHandler.loginSuccess(data, stage);

            case LOGIN_FAILED ->
                    AuthHandler.loginFailed(data, stage);

            case REGISTER_SUCCESS ->
                    AuthHandler.registerSuccess(stage);

            case REGISTER_FAILED ->
                    AuthHandler.registerFailed(data, stage);

            case FORGOT_SUCCESS ->
                    AuthHandler.forgotSuccess(stage);

            case FORGOT_FAILED ->
                    AuthHandler.forgotFailed(data, stage);


            // ===== AUCTION =====

            case LIST_SUCCESS ->
                    AuctionHandler.list(data);

            case LIST_EMPTY ->
                    AuctionHandler.listEmpty();

            case CREATE_SUCCESS ->
                    AuctionHandler.createSuccess(stage);

            case CREATE_FAILED ->
                    AuctionHandler.createFailed(data, stage);

            case JOIN_SUCCESS ->
                    AuctionHandler.joinSuccess(data, stage);

            case JOIN_FAILED ->
                    AuctionHandler.joinFailed(data, stage);

            case BID_SUCCESS ->
                    AuctionHandler.bidSuccess(stage);

            case BID_FAILED ->
                    AuctionHandler.bidFailed(data, stage);

            case BID_HISTORY_SUCCESS ->
                    AuctionHandler.bidHistory(data);

            case BID_HISTORY_EMPTY ->
                    AuctionHandler.bidHistoryEmpty();

            case LIST_MY_AUCTIONS_SUCCESS ->
                    AuctionHandler.myAuctions(data);

            case LIST_MY_AUCTIONS_EMPTY ->
                    AuctionHandler.myAuctionsEmpty();

            case LIST_JOINED_AUCTIONS_SUCCESS ->
                    AuctionHandler.joinedAuctions(data);

            case LIST_JOINED_AUCTIONS_EMPTY ->
                    AuctionHandler.joinedAuctionsEmpty();


            // ===== FAVOURITE =====

            case LIST_FAVOURITES_SUCCESS ->
                    FavouriteHandler.load(data);

            case LIST_FAVOURITES_EMPTY ->
                    FavouriteHandler.empty();

            case ADD_FAVOURITE_SUCCESS ->
                    FavouriteHandler.addSuccess(stage);

            case ADD_FAVOURITE_FAILED ->
                    FavouriteHandler.addFailed(data, stage);

            case REMOVE_FAVOURITE_SUCCESS ->
                    FavouriteHandler.remove(data);

            case REMOVE_FAVOURITE_FAILED ->
                    FavouriteHandler.removeFailed(data, stage);


            // ===== BALANCE =====

            case BALANCE_UPDATE_SUCCESS ->
                    BalanceHandler.success(
                            type.name() + "|" + data,
                            stage
                    );

            case BALANCE_UPDATE_FAILED ->
                    BalanceHandler.failed(data, stage);

            case TRANSACTIONS_LIST ->
                    BalanceHandler.transactions(data);


            // ===== ITEM =====

            case ITEM_LIST_SUCCESS ->
                    AuctionHandler.items(data);

            case ITEM_LIST_EMPTY ->
                    AuctionHandler.itemsEmpty();

            case DELETE_ITEM_SUCCESS ->
                    AuctionHandler.deleteSuccess(stage);

            case DELETE_ITEM_FAILED ->
                    AuctionHandler.deleteFailed(data, stage);

            case UPDATE_ITEM_SUCCESS ->
                    AuctionHandler.updateSuccess(stage);

            case UPDATE_ITEM_FAILED ->
                    AuctionHandler.updateFailed(data, stage);


            // ===== ADMIN USER =====

            case USER_LIST_SUCCESS ->
                    AdminHandler.users(data);

            case USER_LIST_EMPTY ->
                    AdminHandler.usersEmpty();

            case DELETE_USER_SUCCESS ->
                    AdminHandler.deleteSuccess(stage);

            case DELETE_USER_FAILED ->
                    AdminHandler.deleteFailed(data, stage);

            case UPDATE_USER_ROLE_SUCCESS ->
                    AdminHandler.updateRoleSuccess(stage);

            case UPDATE_USER_ROLE_FAILED ->
                    AdminHandler.updateRoleFailed(data, stage);


            // ===== ADMIN AUCTION =====

            case AUCTION_HISTORY_SUCCESS ->
                    AdminHandler.auctionHistory(data);

            case AUCTION_HISTORY_EMPTY ->
                    AdminHandler.auctionHistoryEmpty();

            case ALL_AUCTIONS_SUCCESS ->
                    AdminHandler.auctions(data);

            case ALL_AUCTIONS_EMPTY ->
                    AdminHandler.auctionsEmpty();

            case PENDING_AUCTIONS_SUCCESS ->
                    AdminHandler.pendingAuctions(data);

            case PENDING_AUCTIONS_EMPTY ->
                    AdminHandler.pendingAuctionsEmpty();

            case APPROVE_AUCTION_SUCCESS ->
                    AdminHandler.approveSuccess(stage);

            case APPROVE_AUCTION_FAILED ->
                    AdminHandler.approveFailed(data, stage);

            case STOP_AUCTION_SUCCESS,
                 RESUME_AUCTION_SUCCESS,
                 CANCEL_AUCTION_SUCCESS ->
                    AdminHandler.auctionActionSuccess(stage);

            case STOP_AUCTION_FAILED ->
                    AdminHandler.stopFailed(data, stage);

            case RESUME_AUCTION_FAILED ->
                    AdminHandler.resumeFailed(data, stage);

            case CANCEL_AUCTION_FAILED ->
                    AdminHandler.cancelFailed(data, stage);


            // ===== SYSTEM =====

            case ERROR ->
                    NavigationUtils.showError(
                            "System Error: " + data
                    );

            case DISCONNECTED ->
                    NavigationUtils.showError(
                            "Connection Lost"
                    );

            default ->
                    System.out.println(
                            "Unhandled Response: " + type
                    );
        }
    }
}
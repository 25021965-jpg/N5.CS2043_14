package client.network.response;

import client.controller.UserLiveAuctionController;
import client.manager.AuctionStateManager;
import client.manager.ControllerRegistry;
import client.manager.UserSession;
import client.network.response.handler.*;
import client.util.AlertUtils;
import common.ResponseType;
import javafx.application.Platform;
import javafx.stage.Stage;
import model.ParticipationStatus;
import model.Entity.User.User;

public class ResponseRouter {

    public static void route(String raw, Stage stage) {

        // ===== LIVE UPDATE (xử lý riêng vì không có ResponseType) =====
        if (raw.startsWith("UPDATE_PRICE")) {
            System.out.println(" [ResponseRouter] Routing UPDATE_PRICE to AuctionHandler");
            Platform.runLater(() -> AuctionHandler.updatePrice(raw));
            return;
        }

        // THÊM XỬ LÝ JOIN_SUCCESS
        if (raw.startsWith("JOIN_SUCCESS")) {
            System.out.println(" [ResponseRouter] Routing JOIN_SUCCESS");
            Platform.runLater(() -> AuctionHandler.joinSuccess(raw.substring("JOIN_SUCCESS|".length())));
            return;
        }

        if (raw.startsWith("YOU_WON")) {
            // Set WON state trực tiếp, không cần controller
            String[] parts = raw.split("\\|");
            // YOU_WON|auctionId|finalPrice|winnerUsername (tuỳ format server)
            // Cần biết auctionId → xem format server gửi
            Platform.runLater(() -> {
                UserLiveAuctionController ctrl =
                        ControllerRegistry.get(UserLiveAuctionController.class);
                if (ctrl != null) ctrl.handleServerMessage(raw);
            });
            return;
        }

        // Trong ResponseRouter
        if (raw.startsWith("AUCTION_ENDED")) {
            Platform.runLater(() -> {
                // Set state dựa vào globalAuctionId và globalWinnerName
                String auctionId = UserLiveAuctionController.getGlobalAuctionId();
                String winnerName = UserLiveAuctionController.getGlobalWinnerName();
                User currentUser = UserSession.getCurrentUser();

                if (auctionId != null && currentUser != null && winnerName != null) {
                    ParticipationStatus status =
                            winnerName.equals(currentUser.getUsername())
                                    ? ParticipationStatus.WON
                                    : ParticipationStatus.LOST;
                    AuctionStateManager.setParticipation(auctionId, status);
                }

                UserLiveAuctionController ctrl =
                        ControllerRegistry.get(UserLiveAuctionController.class);
                if (ctrl != null) ctrl.handleServerMessage(raw);
            });
            return;
        }

        // THÊM XỬ LÝ TIME_EXTENDED
        if (raw.startsWith("TIME_EXTENDED")) {
            Platform.runLater(() -> ControllerRegistry.get(UserLiveAuctionController.class).handleServerMessage(raw));
            return;
        }


        // THÊM XỬ LÝ BID_HISTORY
        if (raw.startsWith("BID_HISTORY_SUCCESS")) {
            Platform.runLater(() -> AuctionHandler.bidHistory(raw.substring("BID_HISTORY_SUCCESS|".length())));
            return;
        }

        if (raw.startsWith("BID_HISTORY_EMPTY")) {
            Platform.runLater(AuctionHandler::bidHistoryEmpty);
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
                    AuthHandler.loginFailed();

            case REGISTER_SUCCESS ->
                    AuthHandler.registerSuccess(stage);

            case REGISTER_FAILED ->
                    AuthHandler.registerFailed(data);

            case FORGOT_SUCCESS ->
                    AuthHandler.forgotSuccess(stage);

            case FORGOT_FAILED ->
                    AuthHandler.forgotFailed(data);


            // ===== AUCTION =====

            case LIST_SUCCESS ->
                    AuctionHandler.list(data);

            case LIST_EMPTY ->
                    AuctionHandler.listEmpty();

            case CREATE_SUCCESS ->
                    AuctionHandler.createSuccess(stage);

            case CREATE_FAILED ->
                    AuctionHandler.createFailed(data);

            case JOIN_FAILED ->
                    AuctionHandler.joinFailed(data);

            case BID_SUCCESS ->
                    AuctionHandler.bidSuccess(stage);

            case BID_FAILED ->
                    AuctionHandler.bidFailed(data);

            case LIST_CREATED_AUCTIONS_SUCCESS ->
                    AuctionHandler.CreatedAuctions(data);

            case LIST_CREATED_AUCTIONS_EMPTY ->
                    AuctionHandler.CreatedAuctionsEmpty();

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
                    FavouriteHandler.addFailed(data);

            case REMOVE_FAVOURITE_SUCCESS ->
                    FavouriteHandler.removeSuccess(data);

            case REMOVE_FAVOURITE_FAILED ->
                    FavouriteHandler.removeFailed(data);


            // ===== BALANCE =====

            case BALANCE_UPDATE_SUCCESS ->
                    BalanceHandler.success(
                            type.name() + "|" + data,
                            stage
                    );

            case BALANCE_UPDATE_FAILED ->
                    BalanceHandler.failed(data);

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
                    AuctionHandler.deleteFailed(data);

            case UPDATE_ITEM_SUCCESS ->
                    AuctionHandler.updateSuccess(stage);

            case UPDATE_ITEM_FAILED ->
                    AuctionHandler.updateFailed(data);


            // ===== ADMIN USER =====

            case USER_LIST_SUCCESS ->
                    AdminHandler.users(data);

            case USER_LIST_EMPTY ->
                    AdminHandler.usersEmpty();

            case DELETE_USER_SUCCESS ->
                    AdminHandler.deleteSuccess(stage);

            case DELETE_USER_FAILED ->
                    AdminHandler.deleteFailed(data);

            case UPDATE_USER_ROLE_SUCCESS ->
                    AdminHandler.updateRoleSuccess(stage);

            case UPDATE_USER_ROLE_FAILED ->
                    AdminHandler.updateRoleFailed(data);


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
                    AdminHandler.approveFailed(data);

            case STOP_AUCTION_SUCCESS,
                 RESUME_AUCTION_SUCCESS,
                 CANCEL_AUCTION_SUCCESS ->
                    AdminHandler.auctionActionSuccess(stage);

            case STOP_AUCTION_FAILED ->
                    AdminHandler.stopFailed(data);

            case RESUME_AUCTION_FAILED ->
                    AdminHandler.resumeFailed(data);

            case CANCEL_AUCTION_FAILED ->
                    AdminHandler.cancelFailed(data);

            case AUCTION_STATES_SUCCESS ->
                    AuctionHandler.auctionStates(data);
            case AUCTION_STATES_FAILED ->
                    System.out.println("Load auction states failed");


            // ===== SYSTEM =====

            case ERROR ->
                    AlertUtils.error("System Error: " + data);

            case DISCONNECTED ->
                    AlertUtils.error("Connection Lost");
            default ->
                    System.out.println("Unhandled Response: " + type);
        }
    }
}
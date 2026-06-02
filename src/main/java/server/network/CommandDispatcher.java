package server.network;

import common.Command;
import server.network.handler.*;

public class CommandDispatcher {

    private final AuthHandler authHandler;
    private final AuctionHandler auctionHandler;
    private final FavouriteHandler favouriteHandler;
    private final BalanceHandler balanceHandler;
    private final AdminHandler adminHandler;

    public CommandDispatcher(AuthHandler authHandler,
                             AuctionHandler auctionHandler,
                             FavouriteHandler favouriteHandler,
                             BalanceHandler balanceHandler,
                             AdminHandler adminHandler) {

        this.authHandler = authHandler;
        this.auctionHandler = auctionHandler;
        this.favouriteHandler = favouriteHandler;
        this.balanceHandler = balanceHandler;
        this.adminHandler = adminHandler;
    }

    public String dispatch(Command cmd, String[] data) {

        switch (cmd) {

            // ================= AUTH =================

            case LOGIN:
                return authHandler.handleLogin(data);

            case REGISTER:
                return authHandler.handleRegister(data);

            case FORGOT_PASSWORD:
                return authHandler.handleForgotPassword(data);

            case LOGOUT:
                return authHandler.handleLogout();

            // ================= AUCTION =================

            case CREATE:
                return authHandler.handleCreate(data);

            case LIST:
                return auctionHandler.handleList();

            case JOIN:
                return auctionHandler.handleJoin(data);

            case LEAVE:
                return auctionHandler.handleLeave();

            case BID:
                return auctionHandler.handleBid(data);

            case GET_BID_HISTORY:
                return auctionHandler.handleGetBidHistory(data);

            case LIST_MY_AUCTIONS:
                return auctionHandler.handleListMyAuctions();

            case LIST_JOINED_AUCTIONS:
                return auctionHandler.handleListJoinedAuctions();

            // ================= FAVOURITE =================

            case ADD_FAVOURITE:
                return favouriteHandler.handleAddFavourite(data);

            case REMOVE_FAVOURITE:
                return favouriteHandler.handleRemoveFavourite(data);

            case LIST_FAVOURITES:
                return favouriteHandler.handleListFavourites();

            // ================= BALANCE =================

            case GET_BALANCE:
                return balanceHandler.handleGetBalance(data);

            case DEPOSIT:
                return balanceHandler.handleDeposit(data);

            case WITHDRAW:
                return balanceHandler.handleWithdraw(data);

            case GET_TRANSACTIONS:
                return balanceHandler.handleGetTransactions(data);

            case GET_VIRTUAL_BALANCE:
                return balanceHandler.handleGetVirtualBalance(data);

            // ================= ADMIN =================

            case LIST_USERS:
                return adminHandler.handleListUsers();

            case DELETE_USER:
                return adminHandler.handleDeleteUser(data);

            case UPDATE_USER_ROLE:
                return adminHandler.handleUpdateUserRole(data);

            case LIST_ITEMS:
                return adminHandler.handleListItems();

            case DELETE_ITEM:
                return adminHandler.handleDeleteItem(data);

            case UPDATE_ITEM:
                return adminHandler.handleUpdateItem(data);

            case LIST_AUCTION_HISTORY:
                return adminHandler.handleListAuctionHistory();

            case LIST_ALL_AUCTIONS:
                return adminHandler.handleListAllAuctions();

            case STOP_AUCTION:
                return adminHandler.handleStopAuction(data);

            case RESUME_AUCTION:
                return adminHandler.handleResumeAuction(data);

            case CANCEL_AUCTION:
                return adminHandler.handleCancelAuction(data);

            case APPROVE_AUCTION:
                return adminHandler.handleApproveAuction(data);

            case LIST_PENDING_AUCTIONS:
                return adminHandler.handleListPendingAuctions();

            default:
                return "ERROR|Unsupported command";
        }
    }
}

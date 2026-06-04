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

        return switch (cmd) {

            // ================= AUTH =================
            case LOGIN -> authHandler.handleLogin(data);
            case REGISTER -> authHandler.handleRegister(data);
            case FORGOT_PASSWORD -> authHandler.handleForgotPassword(data);
            case LOGOUT -> authHandler.handleLogout();

            // ================= AUCTION =================

            case CREATE -> authHandler.handleCreate(data);
            case LIST -> auctionHandler.handleList();
            case JOIN -> auctionHandler.handleJoin(data);
            case LEAVE -> auctionHandler.handleLeave();
            case BID -> auctionHandler.handleBid(data);
            case GET_BID_HISTORY -> auctionHandler.handleGetBidHistory(data);
            case LIST_CREATED_AUCTIONS -> auctionHandler.handleListCreatedAuctions();
            case LIST_JOINED_AUCTIONS -> auctionHandler.handleListJoinedAuctions();
            case SET_AUTO_BID -> auctionHandler.handleSetAutoBid(data);
            case CANCEL_AUTO_BID -> auctionHandler.handleCancelAutoBid(data);
            case LOAD_AUCTION_STATES -> auctionHandler.handleLoadAuctionStates();

            // ================= FAVOURITE =================

            case ADD_FAVOURITE -> favouriteHandler.handleAddFavourite(data);
            case REMOVE_FAVOURITE -> favouriteHandler.handleRemoveFavourite(data);
            case LIST_FAVOURITES -> favouriteHandler.handleListFavourites();

            // ================= BALANCE =================

            case GET_BALANCE -> balanceHandler.handleGetBalance(data);
            case DEPOSIT -> balanceHandler.handleDeposit(data);
            case WITHDRAW -> balanceHandler.handleWithdraw(data);
            case GET_TRANSACTIONS -> balanceHandler.handleGetTransactions(data);
            case GET_VIRTUAL_BALANCE -> balanceHandler.handleGetVirtualBalance(data);

            // ================= ADMIN =================

            case LIST_USERS -> adminHandler.handleListUsers();
            case DELETE_USER -> adminHandler.handleDeleteUser(data);
            case UPDATE_USER_ROLE -> adminHandler.handleUpdateUserRole(data);
            case LIST_ITEMS -> adminHandler.handleListItems();
            case DELETE_ITEM -> adminHandler.handleDeleteItem(data);
            case UPDATE_ITEM -> adminHandler.handleUpdateItem(data);
            case LIST_AUCTION_HISTORY -> adminHandler.handleListAuctionHistory();
            case LIST_ALL_AUCTIONS -> adminHandler.handleListAllAuctions();
            case STOP_AUCTION -> adminHandler.handleStopAuction(data);
            case RESUME_AUCTION -> adminHandler.handleResumeAuction(data);
            case CANCEL_AUCTION -> adminHandler.handleCancelAuction(data);
            case APPROVE_AUCTION -> adminHandler.handleApproveAuction(data);
            case GET_AUCTION -> adminHandler.handleGetAuction(data);
            case LIST_PENDING_AUCTIONS -> adminHandler.handleListPendingAuctions();
            default -> "ERROR|Unsupported command";
        };
    }
}

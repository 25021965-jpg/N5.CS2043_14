package client.service;

public class LiveAuctionMessageHandler {

    private final LiveAuctionMessageListener listener;

    public LiveAuctionMessageHandler(LiveAuctionMessageListener listener) {
        this.listener = listener;
    }

    public void handleMessage(String msg) {
        if (msg == null || listener == null) {
            return;
        }

        if (msg.startsWith("AUTO_BID_MAX_REACHED")) {
            listener.onAutoBidMaxReached();
            return;
        }

        if (msg.startsWith("AUTO_BID_CANCELLED")) {
            listener.onAutoBidCancelled();
            return;
        }

        if (msg.startsWith("UPDATE_PRICE")) {
            listener.onUpdatePrice(msg);
            return;
        }

        if (msg.startsWith("VIRTUAL_BALANCE")) {
            listener.onVirtualBalance(msg);
            return;
        }

        if (msg.startsWith("JOIN_SUCCESS")) {
            listener.onJoinSuccess(msg);
            return;
        }

        if (msg.startsWith("JOIN_FAILED")) {
            listener.onJoinFailed(msg);
            return;
        }

        if (msg.startsWith("BID_HISTORY_SUCCESS")) {
            listener.onBidHistorySuccess(msg);
            return;
        }

        if (msg.startsWith("BID_HISTORY_EMPTY")) {
            listener.onBidHistoryEmpty();
            return;
        }

        if (msg.startsWith("BID_FAILED")) {
            listener.onBidFailed(msg);
            return;
        }

        if (msg.startsWith("TIME_EXTENDED")) {
            listener.onTimeExtended(msg);
            return;
        }

        if (msg.startsWith("AUCTION_ENDED")) {
            listener.onAuctionEnded();
            return;
        }

        if (msg.startsWith("YOU_WON")) {
            listener.onYouWon(msg);
            return;
        }

        if (msg.startsWith("WINNER_BALANCE") || msg.startsWith("SELLER_BALANCE")) {
            listener.onSettlementBalance(msg);
            return;
        }

        if (msg.startsWith("ERROR")) {
            listener.onError("Server error: " + msg);
        } else if (msg.startsWith("DISCONNECTED")) {
            listener.onDisconnected();
        }
    }

    public interface LiveAuctionMessageListener {
        void onAutoBidMaxReached();
        void onAutoBidCancelled();
        void onUpdatePrice(String msg);
        void onVirtualBalance(String msg);
        void onJoinSuccess(String msg);
        void onJoinFailed(String msg);
        void onBidHistorySuccess(String msg);
        void onBidHistoryEmpty();
        void onBidFailed(String msg);
        void onTimeExtended(String msg);
        void onAuctionEnded();
        void onYouWon(String msg);
        void onSettlementBalance(String msg);
        void onError(String errorMessage);
        void onDisconnected();
    }
}

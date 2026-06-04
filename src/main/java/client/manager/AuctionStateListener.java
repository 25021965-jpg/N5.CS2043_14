package client.manager;

public interface AuctionStateListener {
    void onAuctionStateChanged(String auctionId);
}
// ============================================================
// MỤC ĐÍCH: Định nghĩa interface Observer cho pattern Observer.
// Các observer đại diện cho client connection
// sẽ implement interface này để nhận thông báo.
// ============================================================
package server.observer;

public interface AuctionObserver {
    void onAuctionEvent(String auctionId, String message);
}
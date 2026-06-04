// ============================================================
// MỤC ĐÍCH: Định nghĩa interface Subject trong Observer Pattern.
//           RoomManager sẽ implement interface này.
// ============================================================
package server.observer;

public interface AuctionSubject {

    void addObserver(String auctionId, AuctionObserver observer);

    void removeObserver(String auctionId, AuctionObserver observer);

    void notifyObservers(String auctionId, String message);
}
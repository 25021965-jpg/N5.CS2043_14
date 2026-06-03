package model.Entity.User;

/**
 * Người đặt giá — có thể tham gia đấu giá bất kỳ phiên nào.
 */
public class Bidder extends User {

    public Bidder() {
        setRole(Role.BIDDER);
    }

    @Override
    public String getDefaultAction() {
        return "Place bid";
    }

    @Override
    public String getSummary() {
        return "[BIDDER] " + getUsername();
    }
}
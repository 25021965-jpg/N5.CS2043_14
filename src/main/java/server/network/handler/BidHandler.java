package server.network.handler;

import model.Auction;
import model.Entity.User.User;
import server.manager.RoomManager;
import server.service.AuctionService;
import server.service.AutoBidService;

import java.io.PrintWriter;
import java.math.BigDecimal;

public class BidHandler extends BaseHandler {

    public BidHandler(User currentUser, PrintWriter writer, String currentAuctionId) {
        super(currentUser, writer, currentAuctionId);
    }

    /**
     * Đăng ký auto-bid.
     *
     * <p><b>FIX:</b> Sau khi set thành công, broadcast "AUTO_BID_ACTIVE" đến
     * tất cả client trong phòng để các người tham gia biết có auto-bidder.
     * Client tự quyết có hiển thị indicator hay không (UI concern).
     */
    public String setAutoBid(String[] data) {
        try {
            // Protocol: SET_AUTO_BID|auctionId|maxAmount
            String auctionId = data[1];
            BigDecimal maxAmount = new BigDecimal(data[2]);

            Auction auction = AuctionService.getAuctionById(auctionId);
            if (auction == null) return "ERROR|Auction not found";

            // Ngăn seller tự bid cho auction của mình
            if (auction.getSeller() != null
                    && auction.getSeller().getUser_id().equals(currentUser.getUser_id())) {
                return "ERROR|You cannot auto-bid on your own auction!";
            }

            String result = AutoBidService.setAutoBid(currentUser, auction, maxAmount);

            // ── FIX: Broadcast thông báo auto-bid đang active ────────────
            if (result.startsWith("AUTO_BID_SET") || result.startsWith("BID_SUCCESS")) {
                RoomManager.broadcastToRoomAll(
                        auctionId,
                        "AUTO_BID_ACTIVE|" + currentUser.getUsername()
                );
            }
            // ─────────────────────────────────────────────────────────────

            return result;

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    /**
     * Hủy auto-bid.
     *
     * <p><b>FIX:</b> Sau khi cancel thành công, broadcast thông báo hủy
     * để các client cập nhật UI (ẩn auto-bid indicator nếu đang hiển thị).
     */
    public String cancelAutoBid(String[] data) {
        try {
            String auctionId = data[1];
            String result = AutoBidService.cancelAutoBid(auctionId, currentUser.getUser_id());

            // ── FIX: Broadcast thông báo hủy auto-bid ────────────────────
            if ("AUTO_BID_CANCELLED".equals(result)) {
                RoomManager.broadcastToRoomAll(
                        auctionId,
                        "AUTO_BID_CANCELLED_NOTIFY|" + currentUser.getUsername()
                );
            }
            // ─────────────────────────────────────────────────────────────

            return result;

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }
}
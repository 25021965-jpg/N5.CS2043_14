package server.service;

import model.*;
import server.dao.UserDAO;
import server.dao.AuctionDAO;

public class AdminService {

    // Khóa tài khoản người dùng
    public static boolean banUser(User admin, User targetUser) {
        if (!isAdmin(admin)) return false;

        targetUser.setVerified(false);
        UserDAO.save(targetUser); // UserDAO.save update
        System.out.println("[ADMIN] User " + targetUser.getUsername() + " has been banned.");
        return true;
    }

    // Kích hoạt lại tài khoản
    public static boolean verifyUser(User admin, User targetUser) {
        if (!isAdmin(admin)) return false;

        targetUser.setVerified(true);
        UserDAO.save(targetUser);
        return true;
    }

    // 3. Hủy bỏ một phiên đấu giá (Ví dụ: do vi phạm chính sách)
    public static boolean forceCancelAuction(User admin, String auctionId) {
        if (!isAdmin(admin)) return false;

        AuctionDAO.cancelAuction(auctionId);
        System.out.println("[ADMIN] Auction " + auctionId + " has been removed by Admin.");
        return true;
    }

    // Hàm kiểm tra quyền nội bộ
    private static boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }
}
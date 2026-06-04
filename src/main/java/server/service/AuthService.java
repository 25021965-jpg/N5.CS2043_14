package server.service;

import model.Entity.User.User;
import server.dao.UserDAO;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    // Map này dùng để quản lý những ai đang online
    private static final Map<String, User> sessions = new ConcurrentHashMap<>();

    // Register
    public static User register(String fullname, String username, String email, String password, String dob) {
        // 1. Kiểm tra xem username hoặc email đã tồn tại chưa
        if (UserDAO.findByUsernameOrEmail(username) != null || UserDAO.findByUsernameOrEmail(email) != null) {
            System.out.println("Register failed: Username or Email existed");
            return null;
        }

        // 2. Gọi DAO để lưu
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        boolean success = UserDAO.register(fullname, username, email, hashedPassword, dob);

        if (success) {
            return UserDAO.findByUsernameOrEmail(username);
        }
        return null;
    }

    // Login
    public static User login(String input, String password) {
        User user = UserDAO.findByUsernameOrEmail(input);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public static boolean resetPassword(String fullname, String dob, String username, String email, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        return UserDAO.resetPassword(fullname, dob, username, email, hashedPassword);
    }

    public static void logout(String s) {
    }
}
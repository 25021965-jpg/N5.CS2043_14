package server.service;

import model.User;
import server.dao.UserDAO;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        boolean success = UserDAO.register(fullname, username, email, password, dob);

        if (success) {
            return UserDAO.findByUsernameOrEmail(username);
        }
        return null;
    }

    // Login
    public static User login(String input, String password) {
        // Kiểm tra thông tin đăng nhập qua DAO
        if (UserDAO.login(input, password)) {
            return UserDAO.findByUsernameOrEmail(input);
        }
        return null;
    }

    // --- Quản lý Session tập trung ---

    public static void addSession(String sessionId, User user) {
        sessions.put(sessionId, user);
    }

    public static User getUser(String sessionId) {
        return sessions.get(sessionId);
    }

    public static void logout(String sessionId) {
        sessions.remove(sessionId);
    }

    public static List<User> getAllUsersFromDB() {
        return UserDAO.findAll();
    }
}
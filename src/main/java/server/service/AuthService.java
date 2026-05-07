package server.service;

import model.Role;
import model.User;
import server.dao.UserDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {

    private final Map<String, User> sessions = new ConcurrentHashMap<>();
    private final UserDAO userDAO = new UserDAO();

    public AuthService() {
    }

    // Register
    public User register(String username, String email, String password, List<Role> roles) {
        // 1. Kiểm tra email đã tồn tại chưa bằng SQL
        if (userDAO.findByEmail(email) != null) {
            return null;
        }

        // 2. Tạo User mới (ID có thể dùng UUID hoặc để DB tự tăng)
        String id = UUID.randomUUID().toString();
        User newUser = new User(id, username, email, password, roles);

        // 3. Lưu trực tiếp vào Database thông qua DAO
        userDAO.save(newUser);

        return newUser;
    }

    // Login
    public User login(String email, String password) {
        // Tìm user theo email trong DB
        User user = userDAO.findByEmail(email);

        // Kiểm tra password
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }

    // Quản lý Session
    public void addSession(String clientId, User user) {
        sessions.put(clientId, user);
    }

    public User getUser(String clientId) {
        return sessions.get(clientId);
    }

    public void logout(String clientId) {
        sessions.remove(clientId);
    }

    public boolean isLoggedIn(String clientId) {
        return sessions.containsKey(clientId);
    }

    // Nếu cần lấy tất cả user (ví dụ trang Admin)
    public List<User> getUsers() {
        return userDAO.findAll();
    }
}
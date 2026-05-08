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
    public User register(
            String fullname,
            String username,
            String email,
            String password
    ) {

        // kiểm tra email tồn tại
        if (userDAO.findByEmail(email) != null) {
            return null;
        }

        // tạo user mới
        User user = new User();

        user.setId(UUID.randomUUID().toString());
        user.setFullname(fullname);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        // lưu DB
        userDAO.save(user);

        return user;
    }

    // Login
    public User login(String input, String password) {

        User user =
                userDAO.findByUsernameOrEmail(input);

        if (
                user != null &&
                        user.getPassword().equals(password)
        ) {

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
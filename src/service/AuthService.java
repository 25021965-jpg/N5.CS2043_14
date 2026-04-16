package service;

import model.User;
import model.Role;

import java.util.*;

public class AuthService {

    private List<User> users = new ArrayList<>();

    private Map<String, User> sessions = new HashMap<>();

    public AuthService() {
        // user mẫu
        users.add(new User("admin", "123", List.of(Role.ADMIN)));
        users.add(new User("user", "456", List.of(Role.BIDDER)));
    }

    // login
    public User login(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equals(username)
                    && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    // lưu session
    public void addSession(String clientId, User user) {
        sessions.put(clientId, user);
    }

    // logout
    public void logout(String clientId) {
        sessions.remove(clientId);
    }

    // check login
    public boolean isLoggedIn(String clientId) {
        return sessions.containsKey(clientId);
    }

    // lấy user hiện tại
    public User getUser(String clientId) {
        return sessions.get(clientId);
    }
}
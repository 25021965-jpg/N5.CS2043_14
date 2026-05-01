package service;

import model.Role;
import model.User;

import java.util.*;

public class AuthService {

    private final List<User> users = new ArrayList<>();
    private final Map<String, User> sessions = Collections.synchronizedMap(new HashMap<>());

    public AuthService() {

        // load user từ file
        List<User> loaded = FileService.load("users.dat");

        if (loaded != null && !loaded.isEmpty()) {
            users.addAll(loaded);
        } else {
            // user mặc định
            users.add(new User("1", "admin", "admin@gmail.com", "123",
                    List.of(Role.ADMIN)));

            users.add(new User("2", "user", "user@gmail.com", "456",
                    List.of(Role.BIDDER)));

            FileService.save("users.dat", users);
        }
    }

    // Register
    public User register(String id, String username, String email, String password, List<Role> roles) {

        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return null; // email đã tồn tại
            }
        }

        User newUser = new User(id, username, email, password, roles);
        users.add(newUser);

        FileService.save("users.dat", users);

        return newUser;
    }

    // Login
    public User login(String email, String password) {

        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)
                    && u.getPassword().equals(password)) {

                return u;
            }
        }

        return null;
    }

    // Session: nhớ người dùng
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

    public List<User> getUsers() {
        return users;
    }
}
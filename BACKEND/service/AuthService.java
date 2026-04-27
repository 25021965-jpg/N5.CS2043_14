package service;

import model.Role;
import model.User;
import service.FileService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthService {

    private final List<User> users = new ArrayList<>();
    private final Map<String, User> sessions = new HashMap<>();

    public AuthService() {
        List<User> loaded = FileService.load("users.dat");

        if (loaded != null) {
            users.addAll(loaded);
        } else {
            users.add(new User("1", "admin", "admin@gmail.com", "123",
                    List.of(Role.ADMIN)));

            users.add(new User("2", "user", "user@gmail.com", "456",
                    List.of(Role.BIDDER)));
        }
    }

    public User register(String id, String username, String email, String password, List<Role> roles) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return null;
            }
        }

        User newUser = new User(id, username, email, password, roles);
        users.add(newUser);

        FileService.save("users.dat", users);

        return newUser;
    }

    public User login(String email, String password) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

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
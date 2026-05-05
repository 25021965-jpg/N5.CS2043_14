package server.dao;

import model.User;
import server.service.FileService;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String FILE_PATH = "data/users.dat";

    // LOAD
    public List<User> findAll() {
        List<User> users = FileService.load(FILE_PATH);
        return users != null ? users : new ArrayList<>();
    }

    // SAVE ALL
    public void saveAll(List<User> users) {
        FileService.save(FILE_PATH, users);
    }

    // SAVE 1 USER (thread-safe hơn)
    public synchronized void save(User user) {
        List<User> users = findAll();

        // tránh trùng username
        for (User u : users) {
            if (u.getUsername().equals(user.getUsername())) {
                throw new RuntimeException("User already exists");
            }
        }

        users.add(user);
        saveAll(users);
    }

    // FIND
    public User findByUsername(String username) {
        for (User u : findAll()) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    // EXISTS
    public boolean exists(String username) {
        return findByUsername(username) != null;
    }
}

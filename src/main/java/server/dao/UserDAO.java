package server.dao;

import model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String FILE_PATH = "users.dat";

    // load
    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(FILE_PATH))) {

            return (List<User>) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users:");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // save
    public void saveAll(List<User> users) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {

            oos.writeObject(users);

        } catch (IOException e) {
            System.err.println("Error saving users:");
            e.printStackTrace();
        }
    }

    // save 1 user
    public void save(User user) {
        List<User> users = findAll();
        users.add(user);
        saveAll(users);
    }

    // tìm
    public User findByUsername(String username) {
        List<User> users = findAll();

        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    // check tồn tại
    public boolean exists(String username) {
        return findByUsername(username) != null;
    }
}
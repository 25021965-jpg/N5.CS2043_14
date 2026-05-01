package server.dao;

import model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final String FILE_PATH = "users.dat"; // File lưu dữ liệu người dùng

    // Lưu danh sách người dùng vào file
    public void saveAll(List<User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Đọc danh sách người dùng từ file
    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>(); // Nếu chưa có file thì trả về list rỗng

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            return (List<User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    // Tìm user theo username để phục vụ đăng nhập
    public User findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}
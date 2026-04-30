package service;

import java.io.*;

public class FileService {

    // Save
    public synchronized static <T> void save(String filename, T data) {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(filename))) {

            out.writeObject(data);

        } catch (Exception e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    // Load
    @SuppressWarnings("unchecked")
    public static <T> T load(String filename) {
        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(filename))) {

            return (T) in.readObject();

        } catch (Exception e) {
            return null; // tránh lỗi khi file chưa tồn tại
        }
    }
}
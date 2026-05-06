package server.service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileService {

    // Lock riêng cho từng file
    private static final Map<String, Object> locks = new HashMap<>();

    private static Object getLock(String filename) {
        synchronized (locks) {
            return locks.computeIfAbsent(filename, k -> new Object());
        }
    }

    // SAVE (thread-safe)
    public static <T> void save(String filename, T data) {
        Object lock = getLock(filename);

        synchronized (lock) {
            try {
                File file = new File(filename);
                //Tạo folder nếu chưa có
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
            
            try (ObjectOutputStream out =
                         new ObjectOutputStream(new FileOutputStream(file))) {
                out.writeObject(data);
            }

        } catch (Exception e) {
                System.out.println("Save error: " + e.getMessage());
        }
    }
}

    // LOAD (thread-safe)
    @SuppressWarnings("unchecked")
    public static <T> T load(String filename) {
        Object lock = getLock(filename);

        synchronized (lock) {
            File file = new File(filename);

            if (!file.exists()) {
                return null;
            }

            try (ObjectInputStream in =
                         new ObjectInputStream(new FileInputStream(file))) {

                return (T) in.readObject();

            } catch (Exception e) {
                System.out.println("Load error: " + e.getMessage());
                return null;
            }
        }
    }
}

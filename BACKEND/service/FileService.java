package service;

import java.io.*;
import java.util.List;

public class FileService {

    public static <T> void save(String fileName, List<T> data) {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(fileName))) {

            out.writeObject(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> List<T> load(String fileName) {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(fileName))) {

            return (List<T>) in.readObject();

        } catch (Exception e) {
            return null;
        }
    }
}
package server.dao;

import model.Auction;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {
    private final String FILE_PATH = "auctions.dat";

    public void saveAll(List<Auction> auctions) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(auctions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Auction> findAll() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            return (List<Auction>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
}
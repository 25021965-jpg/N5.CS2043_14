package server.dao;

import model.Auction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    private static final String FILE_PATH = "auctions.dat";

    // save
    public void saveAll(List<Auction> auctions) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {

            oos.writeObject(auctions);

        } catch (IOException e) {
            System.err.println("Error saving auctions:");
            e.printStackTrace();
        }
    }

    // load
    public List<Auction> findAll() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(FILE_PATH))) {

            Object obj = ois.readObject();

            if (obj instanceof List<?>) {
                return (List<Auction>) obj;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading auctions:");
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
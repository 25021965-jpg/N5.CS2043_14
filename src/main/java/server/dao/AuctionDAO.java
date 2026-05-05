package server.dao;

import model.Auction;
import server.service.FileService;

import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    private static final String FILE_PATH = "data/auctions.dat";

    // LOAD
    public List<Auction> findAll() {
        List<Auction> auctions = FileService.load(FILE_PATH);
        return auctions != null ? auctions : new ArrayList<>();
    }

    // SAVE ALL
    public void saveAll(List<Auction> auctions) {
        FileService.save(FILE_PATH, auctions);
    }
}

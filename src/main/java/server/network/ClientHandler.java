package server.network;

import common.Command;
import model.*;
import server.service.*;
import server.dao.UserDAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private User currentUser;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String clientInfo = socket.getInetAddress().toString();
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("[NEW CONNECTION] Client connected from: " + clientInfo);

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {

                System.out.println("[RECEIVE FROM " + (currentUser != null ? currentUser.getUsername() : clientInfo) + "]: " + clientMessage);

                String response = handleRequest(clientMessage);

                System.out.println("[RESPONSE TO CLIENT]: " + (response.length() > 100 ? response.substring(0, 100) + "..." : response));

                writer.println(response);
            }
        } catch (IOException e) {
            System.out.println("[DISCONNECTED] Client " + clientInfo + " left the building.");
        } finally {
            closeConnection();
        }
    }

    private String handleRequest(String message) {
        String[] data = message.split("\\|");
        if (data.length == 0) return "ERROR|Empty command";

        try {
            Command cmd = Command.valueOf(data[0].toUpperCase());

            System.out.println("  → Processing Command: " + cmd);

            switch (cmd) {
                case LOGIN: return handleLogin(data);
                case REGISTER: return handleRegister(data);
                case CREATE: return handleCreate(data);
                case LIST: return handleList();
                case BID: return handleBid(data);
                case LOGOUT:
                    System.out.println("  → User " + (currentUser != null ? currentUser.getUsername() : "Unknown") + " logged out.");
                    this.currentUser = null;
                    return "LOGOUT_SUCCESS";
                default: return "ERROR|Command not supported";
            }
        } catch (IllegalArgumentException e) {
            System.err.println("  ✕ Unknown Command: " + data[0]);
            return "ERROR|Invalid Command";
        } catch (Exception e) {
            System.err.println("  ✕ Server Logic Error: " + e.getMessage());
            return "ERROR|Server Error: " + e.getMessage();
        }
    }

    private String handleLogin(String[] data) {
        if (data.length < 3) return "LOGIN_FAILED";
        System.out.print("  → Attempting login for: " + data[1]);

        User user = AuthService.login(data[1].trim(), data[2].trim());

        if (user != null) {
            this.currentUser = user;
            System.out.println(" [SUCCESS]");
            return "LOGIN_SUCCESS|" + user.getUser_id() + "|" + user.getFullname() + "|"
                    + user.getUsername() + "|" + user.getEmail() + "|"
                    + (user.getDob() == null ? "" : user.getDob()) + "|"
                    + user.getRole().name(); // Gửi thêm Role về để ResponseHandler parse
        }
        System.out.println(" [FAILED]");
        return "LOGIN_FAILED";
    }

    private String handleRegister(String[] data) {
        if (data.length < 6) return "REGISTER_FAILED";
        System.out.println("  → Registering new user: " + data[2]);

        User user = AuthService.register(data[1], data[2], data[3], data[4], data[5]);
        return (user != null) ? "REGISTER_SUCCESS" : "REGISTER_FAILED|Username/Email already exists";
    }

    private String handleCreate(String[] data) {
        try {
            if (this.currentUser == null) return "ERROR|Please login first";
            System.out.println("  → User " + currentUser.getUsername() + " is creating a new auction: " + data[3]);

            Item item = new Item();
            item.setItem_id(data[2]);
            item.setName(data[3]);
            item.setDescription(data[4]);
            item.setCategory(Category.valueOf(data[5].toUpperCase()));

            List<String> imageList = new ArrayList<>();
            if (!data[6].isEmpty() && !data[6].equals("NO_IMAGE")) {
                for (String path : data[6].split(",")) imageList.add(path);
            }
            item.setImages(imageList);

            AuctionService.createAuction(
                    this.currentUser,
                    item,
                    new BigDecimal(data[7]),
                    new BigDecimal(data[8]),
                    data[9],
                    data[10]
            );

            System.out.println("  → Create Success!");
            return "CREATE_SUCCESS";
        } catch (Exception e) {
            System.err.println("  → Create Failed: " + e.getMessage());
            return "CREATE_FAILED|" + e.getMessage();
        }
    }

    private String handleBid(String[] data) {
        if (this.currentUser == null) return "ERROR|Unauthorized";
        try {
            String auctionId = data[1];
            BigDecimal amount = new BigDecimal(data[2]);
            System.out.println("  → User " + currentUser.getUsername() + " bids " + amount + " on Auction " + auctionId);

            Auction auction = AuctionService.getAuctionById(auctionId);
            if (auction == null) return "ERROR|Auction not found";

            String result = BidService.placeBid(this.currentUser, auction, amount);
            System.out.println("  → Bid result: " + result);
            return result;

        } catch (Exception e) {
            System.err.println("  → Bid Error: " + e.getMessage());
            return "BID_FAILED|" + e.getMessage();
        }
    }

    private String handleList() {
        System.out.print("  → Fetching auction list...");
        try {
            List<Auction> auctions = AuctionService.getAllAuctions();
            if (auctions.isEmpty()) {
                System.out.println(" [EMPTY]");
                return "LIST_EMPTY";
            }

            StringBuilder sb = new StringBuilder("LIST_SUCCESS");
            for (Auction a : auctions) {
                if (a.getItem() == null) continue;

                String firstImg = (a.getItem().getImages() != null && !a.getItem().getImages().isEmpty())
                        ? a.getItem().getImages().get(0) : "NO_IMAGE";

                String cleanDesc = (a.getItem().getDescription() != null)
                        ? a.getItem().getDescription().replace(";", ",") : "";

                sb.append("|")
                        .append(a.getAuction_id()).append(";")
                        .append(a.getItem().getName()).append(";")
                        .append(a.getCurrentPrice()).append(";")
                        .append(a.getMinIncrement()).append(";")
                        .append(firstImg).append(";")
                        .append(a.getStartTime()).append(";")
                        .append(a.getEndTime()).append(";")
                        .append(a.getItem().getCategory().name()).append(";")
                        .append(cleanDesc).append(";")
                        .append(a.getStatus().name());
            }
            System.out.println(" [SUCCESS - " + auctions.size() + " items]");
            return sb.toString();
        } catch (Exception e) {
            System.err.println(" [FAILED]");
            return "ERROR|Could not load auctions";
        }
    }

    private void closeConnection() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("[CLOSED] Resources cleaned up for a client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
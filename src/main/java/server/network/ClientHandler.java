package server.network;

import common.Command;
import model.*;
import server.dao.AuctionDAO;
import server.dao.UserDAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private User currentUser; // Dùng để gán seller cho Auction

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                System.out.println("Received from client: " + clientMessage);
                String response = handleRequest(clientMessage);
                System.out.println("Response: " + response);
                writer.println(response);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            closeConnection();
        }
    }

    private String handleRequest(String message) {
        // Sử dụng regex | để bóc tách mảng data
        String[] data = message.split("\\|");
        if (data.length == 0) return "ERROR|Empty command";

        try {
            Command cmd = Command.valueOf(data[0]);

            switch (cmd) {
                case LOGIN:
                    return handleLogin(data);
                case REGISTER:
                    return handleRegister(data);
                case CREATE:
                    return handleCreate(data);
                case LIST:
                    return handleList();
                default:
                    return "ERROR|Command not supported yet";
            }
        } catch (IllegalArgumentException e) {
            return "ERROR|Unknown Command: " + data[0];
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|Server Process Error";
        }
    }

    private String handleLogin(String[] data) {
        if (data.length < 3) return "LOGIN_FAILED";
        String input = data[1].trim();
        String password = data[2].trim();

        if (UserDAO.login(input, password)) {
            this.currentUser = new UserDAO().findByUsernameOrEmail(input);
            if (this.currentUser == null) return "LOGIN_FAILED";

            return "LOGIN_SUCCESS|" + currentUser.getId() + "|" + currentUser.getFullname() + "|"
                    + currentUser.getUsername() + "|" + currentUser.getEmail() + "|"
                    + (currentUser.getDob() == null ? "" : currentUser.getDob());
        }
        return "LOGIN_FAILED";
    }

    private String handleRegister(String[] data) {
        if (data.length < 6) return "REGISTER_FAILED";

        UserDAO userDAO = new UserDAO();
        // Kiểm tra trùng lặp trước khi lưu
        if (userDAO.findByEmail(data[3]) != null || userDAO.findByUsername(data[2]) != null) {
            return "REGISTER_FAILED";
        }

        User user = new User();
        user.setId("USR" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase());
        user.setFullname(data[1]);
        user.setUsername(data[2]);
        user.setEmail(data[3]);
        user.setPassword(data[4]);
        user.setDob(data[5]);

        try {
            userDAO.save(user);
            return "REGISTER_SUCCESS";
        } catch (Exception e) {
            return "REGISTER_FAILED";
        }
    }

    private String handleCreate(String[] data) {
        try {
            if (this.currentUser == null) return "ERROR|Unauthorized. Please login first.";
            if (data.length < 11) return "ERROR|Missing auction data";

            // 1. Map Item
            Item item = new Item();
            item.setId(data[2]);
            item.setName(data[3]);
            item.setDescription(data[4]);
            item.setCategory(Category.valueOf(data[5]));

            List<String> imageList = new ArrayList<>();
            if (!data[6].isEmpty()) {
                for (String path : data[6].split(",")) imageList.add(path);
            }
            item.setImages(imageList);

            // 2. Map Auction
            Auction auction = new Auction();
            auction.setId(data[1]);
            auction.setItem(item);
            auction.setSeller(this.currentUser); // Gán người tạo từ session login
            auction.setCurrentPrice(new BigDecimal(data[7]));
            auction.setMinIncrement(new BigDecimal(data[8]));
            auction.setStartTime(LocalDateTime.parse(data[9]));
            auction.setEndTime(LocalDateTime.parse(data[10]));
            auction.setStatus(AuctionStatus.ACTIVE);

            // 3. Lưu (lưu cả Item và Auction)
            AuctionDAO.save(auction);

            return "CREATE_SUCCESS|Auction created!";
        } catch (Exception e) {
            e.printStackTrace();
            return "CREATE_FAILED|" + e.getMessage();
        }
    }

    private String handleList() {
        try {
            List<Auction> auctions = AuctionDAO.findAll();
            if (auctions.isEmpty()) return "LIST_EMPTY";

            StringBuilder sb = new StringBuilder("LIST_SUCCESS");
            for (Auction a : auctions) {
                if (a.getItem() == null) continue;

                String firstImg = (a.getItem().getImages() != null && !a.getItem().getImages().isEmpty())
                        ? a.getItem().getImages().get(0) : "NO_IMAGE";

                String startTimeStr = (a.getStartTime() != null) ? a.getStartTime().toString() : "";
                String endTimeStr = (a.getEndTime() != null) ? a.getEndTime().toString() : "";
                String categoryStr = (a.getItem().getCategory() != null) ? a.getItem().getCategory().name() : "OTHER";

                sb.append("|")
                        .append(a.getId()).append(";")
                        .append(a.getItem().getName()).append(";")
                        .append(a.getCurrentPrice()).append(";")
                        .append(a.getMinIncrement()).append(";")
                        .append(firstImg).append(";")
                        .append(startTimeStr).append(";")
                        .append(endTimeStr).append(";")
                        .append(categoryStr);

            }
            return sb.toString();
        } catch (Exception e) {
            return "ERROR|Could not load auctions";
        }
    }

    private void closeConnection() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
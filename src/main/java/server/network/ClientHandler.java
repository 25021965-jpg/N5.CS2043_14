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

                if (response == null) {
                    response = "ERROR|Null response";
                }

                System.out.println(
                        "[RESPONSE TO CLIENT]: "
                                + (response.length() > 100
                                ? response.substring(0, 100) + "..."
                                : response)
                );
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

                // --- XỬ LÝ ADMIN ---
                case LIST_USERS: return handleListUsers();
                case DELETE_USER: return handleDeleteUser(data);

                case LOGOUT:
                    System.out.println("  → User " + (currentUser != null ? currentUser.getUsername() : "Unknown") + " logged out.");
                    this.currentUser = null;
                    return "LOGOUT_SUCCESS";
                case FORGOT_PASSWORD: return handleForgotPassword(data);
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

    // --- ADMIN LOGIC ---

    private String handleListUsers() {
        // Chỉ cho phép ADMIN xem danh sách
        if (this.currentUser == null || this.currentUser.getRole() != Role.ADMIN) {
            return "ERROR|Permission denied";
        }

        System.out.print("  → Admin fetching user list...");
        try {
            List<User> users = UserDAO.findAll();
            if (users == null || users.isEmpty()) return "USER_LIST_EMPTY";

            StringBuilder sb = new StringBuilder("USER_LIST_SUCCESS");
            for (User u : users) {
                sb.append("|")
                        .append(u.getUser_id()).append(";")
                        .append(u.getUsername()).append(";")
                        .append(u.getEmail()).append(";")
                        .append(u.getRole().name());
            }
            System.out.println(" [SUCCESS]");
            return sb.toString();
        } catch (Exception e) {
            return "ERROR|DB Error: " + e.getMessage();
        }
    }

    private String handleDeleteUser(String[] data) {
        if (this.currentUser == null || this.currentUser.getRole() != Role.ADMIN) {
            return "ERROR|Permission denied";
        }
        if (data.length < 2) return "DELETE_USER_FAILED|Missing ID";

        String targetId = data[1];
        if (targetId.equals(currentUser.getUser_id())) {
            return "DELETE_USER_FAILED|Cannot delete yourself";
        }

        boolean success = UserDAO.deleteUser(targetId);
        return success ? "DELETE_USER_SUCCESS" : "DELETE_USER_FAILED";
    }

    // --- AUTH & AUCTION LOGIC ---

    private String handleLogin(String[] data) {
        if (data.length < 3) return "LOGIN_FAILED";
        User user = AuthService.login(data[1].trim(), data[2].trim());

        if (user != null) {
            this.currentUser = user;
            return "LOGIN_SUCCESS|" + user.getUser_id() + "|" + user.getFullname() + "|"
                    + user.getUsername() + "|" + user.getEmail() + "|"
                    + (user.getDob() == null ? "" : user.getDob()) + "|"
                    + user.getRole().name() + "|" + user.getBalance();
        }
        return "LOGIN_FAILED";
    }

    private String handleRegister(String[] data) {
        if (data.length < 6) return "REGISTER_FAILED";
        User user = AuthService.register(data[1], data[2], data[3], data[4], data[5]);
        return (user != null) ? "REGISTER_SUCCESS" : "REGISTER_FAILED|Exists";
    }

    private String handleCreate(String[] data) {
        if (data.length < 11) return "CREATE_FAILED|Invalid data";
        try {
            if (this.currentUser == null) return "ERROR|Unauthorized";

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

            // Tự động lên SELLER nếu đang là BIDDER
            if (currentUser.getRole() == Role.BIDDER) {
                new UserDAO().updateRole(currentUser.getUser_id(), "SELLER");
                currentUser.setRole(Role.SELLER);
            }

            AuctionService.createAuction(this.currentUser, item, new BigDecimal(data[7]),
                    new BigDecimal(data[8]), data[9], data[10]);

            return "CREATE_SUCCESS";
        } catch (Exception e) {
            return "CREATE_FAILED|" + e.getMessage();
        }
    }

    private String handleBid(String[] data) {
        if (this.currentUser == null) return "ERROR|Unauthorized";
        try {
            Auction auction = AuctionService.getAuctionById(data[1]);
            if (auction == null) return "ERROR|Not found";
            return BidService.placeBid(this.currentUser, auction, new BigDecimal(data[2]));
        } catch (Exception e) {
            return "BID_FAILED|" + e.getMessage();
        }
    }

    private String handleList() {
        try {
            List<Auction> auctions = AuctionService.getAllAuctions();
            if (auctions == null || auctions.isEmpty()) return "LIST_EMPTY";

            StringBuilder sb = new StringBuilder("LIST_SUCCESS");
            for (Auction a : auctions) {
                Item item = a.getItem();
                if (item == null) continue;
                String firstImg = (item.getImages() != null && !item.getImages().isEmpty()) ? item.getImages().get(0) : "NO_IMAGE";

                sb.append("|").append(a.getAuction_id()).append(";")
                        .append(item.getName()).append(";")
                        .append(a.getCurrentPrice()).append(";")
                        .append(a.getMinIncrement()).append(";")
                        .append(firstImg).append(";")
                        .append(a.getStartTime()).append(";")
                        .append(a.getEndTime()).append(";")
                        .append(item.getCategory().name()).append(";")
                        .append(item.getDescription().replace(";", ",")).append(";")
                        .append(a.getStatus().name());
            }
            return sb.toString();
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }
    private String handleForgotPassword(String[] data) {
        // data[0] = FORGOT_PASSWORD, [1]=fullname, [2]=dob, [3]=username, [4]=email, [5]=newPassword
        if (data.length < 6) return "FORGOT_FAILED|Missing information";

        boolean success = AuthService.resetPassword(
                data[1].trim(),
                data[2].trim(),
                data[3].trim(),
                data[4].trim(),
                data[5].trim()
        );

        return success ? "FORGOT_SUCCESS" : "FORGOT_FAILED|Information does not match";
    }

    private void closeConnection() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
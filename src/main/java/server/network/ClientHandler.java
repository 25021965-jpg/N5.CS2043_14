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
        if (data.length < 11) {
            return "CREATE_FAILED|Invalid auction data";
        }
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

            if (currentUser.getRole() == Role.BIDDER) {

                UserDAO userDAO = new UserDAO();

                userDAO.updateRole(
                        currentUser.getUser_id(),
                        "SELLER"
                );

                currentUser.setRole(Role.SELLER);

                System.out.println("  → Auto upgraded user to SELLER");
            }

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

            if (auctions == null || auctions.isEmpty()) {
                System.out.println(" [EMPTY]");
                return "LIST_EMPTY";
            }

            StringBuilder sb = new StringBuilder("LIST_SUCCESS");

            for (Auction a : auctions) {

                try {

                    if (a == null) continue;

                    Item item = a.getItem();

                    if (item == null) {
                        System.err.println("Auction has null item: " + a.getAuction_id());
                        continue;
                    }

                    String firstImg =
                            (item.getImages() != null
                                    && !item.getImages().isEmpty()
                                    && item.getImages().get(0) != null)
                                    ? item.getImages().get(0)
                                    : "NO_IMAGE";

                    String cleanDesc =
                            (item.getDescription() != null)
                                    ? item.getDescription().replace(";", ",")
                                    : "";

                    String category =
                            (item.getCategory() != null)
                                    ? item.getCategory().name()
                                    : "UNKNOWN";

                    String status =
                            (a.getStatus() != null)
                                    ? a.getStatus().name()
                                    : "UNKNOWN";

                    sb.append("|")
                            .append(a.getAuction_id() != null ? a.getAuction_id() : "NULL").append(";")
                            .append(item.getName() != null ? item.getName() : "Unnamed").append(";")
                            .append(a.getCurrentPrice() != null ? a.getCurrentPrice() : "0").append(";")
                            .append(a.getMinIncrement() != null ? a.getMinIncrement() : "0").append(";")
                            .append(firstImg).append(";")
                            .append(a.getStartTime() != null ? a.getStartTime() : "").append(";")
                            .append(a.getEndTime() != null ? a.getEndTime() : "").append(";")
                            .append(category).append(";")
                            .append(cleanDesc).append(";")
                            .append(status);

                }
                catch (Exception ex) {

                    System.err.println("Auction Parse Error:");

                    if (a != null) {
                        System.err.println("Auction ID = " + a.getAuction_id());
                    }
                    System.err.println("Error: " + ex.getMessage());                }
            }

            System.out.println(" [SUCCESS - " + auctions.size() + " items]");

            return sb.toString();

        }
        catch (Exception e) {

            System.err.println(" [FAILED]");
            System.err.println("Error: " + e.getMessage());
            return "ERROR|Could not load auctions: " + e.getMessage();
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
            System.out.println("[CLOSED] Resources cleaned up for a client.");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());        }
    }
}
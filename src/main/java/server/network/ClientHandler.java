package server.network;

import common.Command;
import model.*;
import server.dao.AuctionDAO;
import server.dao.ItemDAO;
import server.service.*;
import server.dao.UserDAO;

import java.io.BufferedReader;
import client.manager.RoomManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private String currentAuctionId = null;

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
                case JOIN:
                    return handleJoin(data);
                case LEAVE:
                    return handleLeave();
                case GET_BID_HISTORY:
                    return handleGetBidHistory(data);

                // --- XỬ LÝ ADMIN ---
                case LIST_USERS: return handleListUsers();
                case DELETE_USER: return handleDeleteUser(data);
                //-- XU LI ITEM ---
                case LIST_ITEMS:  return handleListItems();
                case DELETE_ITEM: return handleDeleteItem(data);
                case UPDATE_ITEM: return handleUpdateItem(data);


                case LIST_AUCTION_HISTORY: return handleListAuctionHistory();

                case LOGOUT:
                    System.out.println("  → User " + (currentUser != null ? currentUser.getUsername() : "Unknown") + " logged out.");
                    this.currentUser = null;
                    return "LOGOUT_SUCCESS";
                case FORGOT_PASSWORD: return handleForgotPassword(data);
                //manage auctions
                case LIST_ALL_AUCTIONS: return handleListAllAuctions();
                case STOP_AUCTION:      return handleStopAuction(data);
                case RESUME_AUCTION:    return handleResumeAuction(data);
                case CANCEL_AUCTION:    return handleCancelAuction(data);
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

    // ==================== JOIN AUCTION ====================
    private String handleJoin(String[] data) {
        if (data.length < 2) return "JOIN_FAILED|Invalid auction ID";
        String auctionId = data[1];

        Auction auction = AuctionService.getAuctionById(auctionId);
        if (auction == null) {
            return "JOIN_FAILED|Auction not found";
        }

        this.currentAuctionId = auctionId;
        RoomManager.addClient(auctionId, writer);

        System.out.println("  → User " + (currentUser != null ? currentUser.getUsername() : "Guest")
                + " joined auction: " + auctionId);

        return "JOIN_SUCCESS|" + auctionId + "|"
                + auction.getCurrentPrice() + "|"
                + auction.getMinIncrement() + "|"
                + auction.getEndTime();
    }

    // ==================== LEAVE AUCTION ====================
    private String handleLeave() {
        if (currentAuctionId != null) {
            RoomManager.removeClient(currentAuctionId, writer);
            System.out.println("  → User " + (currentUser != null ? currentUser.getUsername() : "Guest")
                    + " left auction: " + currentAuctionId);
            currentAuctionId = null;
        }
        return "LEAVE_SUCCESS";
    }

    // ==================== GET BID HISTORY ====================
    private String handleGetBidHistory(String[] data) {
        if (data.length < 2) return "BID_HISTORY_FAILED|Invalid auction ID";
        String auctionId = data[1];

        List<Bid> history = AuctionDAO.getBidHistory(auctionId);
        if (history.isEmpty()) {
            return "BID_HISTORY_EMPTY";
        }

        StringBuilder sb = new StringBuilder("BID_HISTORY_SUCCESS");
        for (Bid bid : history) {
            sb.append("|").append(bid.getUsername())
                    .append(";").append(bid.getAmount())
                    .append(";").append(bid.getTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
        return sb.toString();
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

    //ITEM

    private String handleListItems() {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "ERROR|Permission denied";

        List<String[]> items = ItemDAO.findAllWithSeller();
        if (items.isEmpty()) return "ITEM_LIST_EMPTY";

        StringBuilder sb = new StringBuilder("ITEM_LIST_SUCCESS");
        for (String[] row : items) {
            // format: item_id;name;category;seller;status
            sb.append("|").append(String.join(";", row));
        }
        return sb.toString();
    }

    private String handleDeleteItem(String[] data) {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "ERROR|Permission denied";
        if (data.length < 2) return "DELETE_ITEM_FAILED|Missing ID";

        boolean ok = ItemDAO.deleteItem(data[1]);
        return ok ? "DELETE_ITEM_SUCCESS" : "DELETE_ITEM_FAILED";
    }

    private String handleUpdateItem(String[] data) {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "ERROR|Permission denied";
        // data: UPDATE_ITEM|item_id|newName|newDescription|newCategory
        if (data.length < 5) return "UPDATE_ITEM_FAILED|Missing data";

        boolean ok = ItemDAO.updateItem(data[1], data[2], data[3], data[4]);
        return ok ? "UPDATE_ITEM_SUCCESS" : "UPDATE_ITEM_FAILED";
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
        if (this.currentAuctionId == null) return "ERROR|You haven't joined any auction";

        try {
            String auctionId = data[1];
            BigDecimal amount = new BigDecimal(data[2]);

            if (!auctionId.equals(this.currentAuctionId)) {
                return "ERROR|You are not in this auction room";
            }

            Auction auction = AuctionService.getAuctionById(auctionId);
            if (auction == null) return "ERROR|Auction not found";

            String result = BidService.placeBid(this.currentUser, auction, amount);

            if (result.startsWith("BID_SUCCESS")) {
                String newPrice = result.split("\\|")[1];
                String broadcastMsg = "UPDATE_PRICE|" + auctionId + "|" + newPrice + "|" + currentUser.getUsername() + "|" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                RoomManager.broadcastToRoomAll(auctionId, broadcastMsg);
            }

            return result;
        } catch (Exception e) {
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
                    if (item == null) continue;

                    // Xử lý ảnh - chỉ lấy URL đầu tiên, loại bỏ base64 dài
                    String firstImg = "NO_IMAGE";
                    if (item.getImages() != null && !item.getImages().isEmpty()) {
                        String img = item.getImages().get(0);
                        // Nếu ảnh quá dài (> 200 ký tự) hoặc chứa base64 thì bỏ qua
                        if (img != null && img.length() < 200 && !img.contains("base64")) {
                            firstImg = img.replace(";", ",").replace("|", "-");
                        }
                    }

                    // Làm sạch dữ liệu: thay thế các ký tự đặc biệt
                    String itemName = (item.getName() != null)
                            ? item.getName().replace(";", ",").replace("|", "-") : "Unnamed";

                    String cleanDesc = (item.getDescription() != null)
                            ? item.getDescription().replace(";", ",").replace("|", "-").replace("\n", " ") : "";

                    String category = (item.getCategory() != null)
                            ? item.getCategory().name() : "UNKNOWN";

                    String status = (a.getStatus() != null)
                            ? a.getStatus().name() : "UNKNOWN";

                    String auctionId = (a.getAuction_id() != null) ? a.getAuction_id() : "NULL";
                    String currentPrice = (a.getCurrentPrice() != null) ? a.getCurrentPrice().toString() : "0";
                    String minIncrement = (a.getMinIncrement() != null) ? a.getMinIncrement().toString() : "0";
                    String startTime = (a.getStartTime() != null) ? a.getStartTime().toString() : "";
                    String endTime = (a.getEndTime() != null) ? a.getEndTime().toString() : "";

                    sb.append("|").append(auctionId).append(";")
                            .append(itemName).append(";")
                            .append(currentPrice).append(";")
                            .append(minIncrement).append(";")
                            .append(firstImg).append(";")
                            .append(startTime).append(";")
                            .append(endTime).append(";")
                            .append(category).append(";")
                            .append(cleanDesc).append(";")
                            .append(status);

                } catch (Exception ex) {
                    System.err.println("Auction Parse Error for ID: " + (a != null ? a.getAuction_id() : "null"));
                    ex.printStackTrace();
                }
            }

            System.out.println(" [SUCCESS - " + auctions.size() + " items]");
            return sb.toString();

        } catch (Exception e) {
            System.err.println(" [FAILED]");
            e.printStackTrace();
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
        if (currentAuctionId != null) {
            RoomManager.removeClient(currentAuctionId, writer);
        }
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
    private String handleListAuctionHistory() {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "ERROR|Permission denied";

        List<String[]> history = AuctionDAO.findAuctionHistory();
        if (history.isEmpty()) return "AUCTION_HISTORY_EMPTY";

        StringBuilder sb = new StringBuilder("AUCTION_HISTORY_SUCCESS");
        for (String[] row : history) {
            // auction_id;item_name;winner;final_bid;end_time;status
            sb.append("|").append(String.join(";", row));
        }
        return sb.toString();
    }
    private String handleListAllAuctions() {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "ERROR|Permission denied";

        List<String[]> auctions = AuctionDAO.findAllAsStrings();
        if (auctions.isEmpty()) return "ALL_AUCTIONS_EMPTY";

        StringBuilder sb = new StringBuilder("ALL_AUCTIONS_SUCCESS");
        for (String[] row : auctions)
            sb.append("|").append(String.join(";", row));
        return sb.toString();
    }

    private String handleStopAuction(String[] data) {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "ERROR|Permission denied";
        if (data.length < 2) return "STOP_AUCTION_FAILED|Missing ID";
        boolean ok = AuctionDAO.stopAuction(data[1]);
        return ok ? "STOP_AUCTION_SUCCESS" : "STOP_AUCTION_FAILED";
    }

    private String handleResumeAuction(String[] data) {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "ERROR|Permission denied";
        if (data.length < 2) return "RESUME_AUCTION_FAILED|Missing ID";
        boolean ok = AuctionDAO.resumeAuction(data[1]);
        return ok ? "RESUME_AUCTION_SUCCESS" : "RESUME_AUCTION_FAILED";
    }

    private String handleCancelAuction(String[] data) {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN)
            return "ERROR|Permission denied";
        if (data.length < 2) return "CANCEL_AUCTION_FAILED|Missing ID";
        AuctionDAO.cancelAuction(data[1]);
        return "CANCEL_AUCTION_SUCCESS";
    }
}
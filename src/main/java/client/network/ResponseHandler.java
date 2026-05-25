package client.network;

import client.controller.*;
import client.manager.UserSession;
import client.util.NavigationUtils;
import common.ResponseType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ResponseHandler {

    private static Stage mainStage;
    private static Consumer<String> liveAuctionListener = null;

    public static void setLiveAuctionListener(Consumer<String> listener) {
        liveAuctionListener = listener;
    }

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void handle(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) return;

        // ========== LIVE UPDATE PRICE (Check trước khi split) ==========
        if (rawMessage.startsWith("UPDATE_PRICE") && liveAuctionListener != null) {
            liveAuctionListener.accept(rawMessage);
            return;
        }

        String[] parts = rawMessage.split("\\|", 2);
        String header = parts[0];
        String data = (parts.length > 1) ? parts[1] : "";

        ResponseType type;
        try {
            type = ResponseType.valueOf(header);
        } catch (Exception e) {
            return;
        }

        Platform.runLater(() -> {
            switch (type) {
                // ================= AUTHENTICATION =================
                case LOGIN_SUCCESS -> {
                    User loginUser = parseUser(data);
                    UserSession.setCurrentUser(loginUser);
                    handleLoginSuccess(loginUser);
                }
                case LOGIN_FAILED ->
                        NavigationUtils.showError(data.isEmpty() ? "Invalid credentials." : data);

                case REGISTER_SUCCESS -> {
                    NavigationUtils.switchScene(mainStage, "/fxml/login-view.fxml", "Login");
                    NavigationUtils.showToast(mainStage, "Account created successfully!");
                }
                case REGISTER_FAILED -> NavigationUtils.showError("Registration failed: " + data);

                case FORGOT_SUCCESS -> {
                    NavigationUtils.switchScene(mainStage, "/fxml/login-view.fxml", "Login");
                    NavigationUtils.showToast(mainStage, "Password reset successfully!");
                }
                case FORGOT_FAILED -> NavigationUtils.showError("Failed to reset password: " + data);

                // ================= CLIENT AUCTION LOGIC =================
                case CREATE_SUCCESS -> {
                    NavigationUtils.showToast(mainStage, "Auction submitted! Waiting for Admin approval.");
                    NavigationUtils.switchScene(mainStage, "/fxml/HomePage.fxml", "Auction Home");
                }
                case CREATE_FAILED -> NavigationUtils.showError("Failed to create auction: " + data);

                case LIST_SUCCESS -> {
                    List<Auction> list = parseAuctionList(data);
                    if (HomePageController.getInstance() != null) {
                        HomePageController.getInstance().updateAuctionList(list);
                    }
                }
                case LIST_EMPTY -> {
                    if (HomePageController.getInstance() != null) {
                        HomePageController.getInstance().updateAuctionList(new ArrayList<>());
                    }
                }

                case BID_SUCCESS -> {
                    NavigationUtils.showToast(mainStage, "Your bid has been placed!");
                    ClientSocket.getInstance().sendList();
                }
                case BID_FAILED -> NavigationUtils.showError("Unable to place bid: " + data);

                case BID_HISTORY_SUCCESS -> {
                    List<Bid> bidHistory = parseBidHistory(data);
                    if (LiveAuctionController.getInstance() != null) {
                        LiveAuctionController.getInstance().loadBidHistory(bidHistory);
                    }
                }

                // ================= ADMIN MANAGEMENT =================

                // --- DUYỆT SẢN PHẨM ---
                case APPROVE_AUCTION_SUCCESS -> {
                    NavigationUtils.showToast(mainStage, "Auction approved successfully!");
                    ClientSocket.getInstance().sendRequest("LIST_PENDING_AUCTIONS");
                }
                case APPROVE_AUCTION_FAILED -> NavigationUtils.showError("Approve failed: " + data);

                case PENDING_AUCTIONS_SUCCESS -> {
                    ObservableList<String[]> items = parseToObservableList(data, 5);
                    if (ManageProductController.getInstance() != null) {
                        ManageProductController.getInstance().updateProducts(items);
                    }
                }

                // --- QUẢN LÝ USER ---
                case USER_LIST_SUCCESS -> {
                    ObservableList<User> users = FXCollections.observableArrayList();
                    for (String token : data.split("\\|")) {
                        String[] f = token.split(";", -1);
                        if (f.length >= 4) {
                            User u = new User();
                            u.setUser_id(f[0]); u.setUsername(f[1]);
                            u.setEmail(f[2]); u.setRole(Role.valueOf(f[3]));
                            users.add(u);
                        }
                    }
                    if (AdminController.getInstance() != null) AdminController.getInstance().updateUsers(users);
                }
                case DELETE_USER_SUCCESS -> {
                    NavigationUtils.showToast(mainStage, "User deleted successfully!");
                    if (AdminController.getInstance() != null) AdminController.getInstance().handleReload();
                }

                // --- QUẢN LÝ ITEM & HISTORY ---
                case ITEM_LIST_SUCCESS -> {
                    ObservableList<String[]> items = parseToObservableList(data, 5);
                    if (ManageProductController.getInstance() != null) ManageProductController.getInstance().updateProducts(items);
                }
                case ALL_AUCTIONS_SUCCESS -> {
                    ObservableList<String[]> listItems = parseToObservableList(data, 5);
                    if (ManageAuctionController.getInstance() != null) ManageAuctionController.getInstance().updateAuctions(listItems);
                }
                case STOP_AUCTION_SUCCESS, RESUME_AUCTION_SUCCESS, CANCEL_AUCTION_SUCCESS -> {
                    NavigationUtils.showToast(mainStage, "Action performed successfully!");
                    if (ManageAuctionController.getInstance() != null) ManageAuctionController.getInstance().handleReloadAuctions();
                }

                // ================= SYSTEM =================
                case ERROR -> NavigationUtils.showError("System error: " + data);
                case DISCONNECTED -> NavigationUtils.showError("Connection lost. Please check your internet connection.");

                default -> System.out.println("Unhandled response type: " + type);
            }
        });
    }

    // --- HELPER PARSE CHUNG ---
    private static ObservableList<String[]> parseToObservableList(String data, int minFields) {
        ObservableList<String[]> items = FXCollections.observableArrayList();
        if (data == null || data.isEmpty()) return items;
        for (String token : data.split("\\|")) {
            String[] fields = token.split(";", -1);
            if (fields.length >= minFields) items.add(fields);
        }
        return items;
    }

    private static void handleLoginSuccess(User loginUser) {
        if (loginUser == null) return;
        String scene = (loginUser.getRole() == Role.ADMIN) ? "/fxml/admin-view.fxml" : "/fxml/HomePage.fxml";
        String title = (loginUser.getRole() == Role.ADMIN) ? "Admin Dashboard" : "Auction Dashboard";

        NavigationUtils.switchScene(mainStage, scene, title);

        if (loginUser.getRole() != Role.ADMIN) {
            Platform.runLater(() -> {
                if (HomePageController.getInstance() != null) HomePageController.getInstance().setUser(loginUser);
            });
        }
        NavigationUtils.showToast(mainStage, "Welcome back, " + loginUser.getFullname() + "!");
    }

    private static List<Auction> parseAuctionList(String data) {
        List<Auction> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;
        for (String token : data.split("\\|")) {
            try {
                String[] p = token.split(";", -1);
                if (p.length < 10) continue;
                Auction a = new Auction();
                a.setAuction_id(p[0]);
                Item item = new Item();
                item.setName(p[1]);
                item.setCategory(Category.valueOf(p[7].toUpperCase()));
                item.setDescription(p[8]);
                if (!p[4].equals("NO_IMAGE")) item.setImages(Collections.singletonList(p[4]));
                a.setItem(item);
                a.setCurrentPrice(new BigDecimal(p[2]));
                a.setMinIncrement(new BigDecimal(p[3]));
                a.setStartTime(LocalDateTime.parse(p[5]));
                a.setEndTime(LocalDateTime.parse(p[6]));
                a.setStatus(AuctionStatus.valueOf(p[9].trim().toUpperCase()));
                if (p.length > 10) a.setSeller_Id(p[10]);
                list.add(a);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return list;
    }

    private static List<Bid> parseBidHistory(String data) {
        List<Bid> history = new ArrayList<>();
        if (data == null || data.isEmpty()) return history;
        for (String token : data.split("\\|")) {
            try {
                String[] parts = token.split(";");
                if (parts.length >= 3) {
                    Bid bid = new Bid();
                    bid.setUsername(parts[0]);
                    bid.setAmount(new BigDecimal(parts[1]));
                    bid.setTimeString(parts[2]);
                    history.add(bid);
                }
            } catch (Exception e) { }
        }
        return history;
    }

    private static User parseUser(String data) {
        try {
            String[] p = data.split("\\|", -1);
            if (p.length < 5) return null;
            User u = new User();
            u.setUser_id(p[0]); u.setFullname(p[1]); u.setUsername(p[2]); u.setEmail(p[3]);
            u.setDob(p[4].isEmpty() ? null : p[4]);
            if (p.length > 5) u.setRole(Role.valueOf(p[5]));
            if (p.length > 6 && !p[6].isEmpty()) u.setBalance(new BigDecimal(p[6]));
            return u;
        } catch (Exception e) { return null; }
    }
}

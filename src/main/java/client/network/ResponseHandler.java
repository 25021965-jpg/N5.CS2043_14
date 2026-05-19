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

    // ========== LIVE AUCTION LISTENER ==========
    private static Consumer<String> liveAuctionListener = null;

    public static void setLiveAuctionListener(Consumer<String> listener) {
        liveAuctionListener = listener;
    }

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void handle(String rawMessage) {

        if (rawMessage == null || rawMessage.isEmpty()) return;

        // ========== LIVE UPDATE PRICE ==========
        if (rawMessage.startsWith("UPDATE_PRICE")
                && liveAuctionListener != null) {

            liveAuctionListener.accept(rawMessage);

            return;
        }

        String[] parts = rawMessage.split("\\|", 2);

        String header = parts[0];

        String data = (parts.length > 1)
                ? parts[1]
                : "";

        ResponseType type;

        try {

            type = ResponseType.valueOf(header);

        } catch (Exception e) {

            return;
        }

        Platform.runLater(() -> {

            switch (type) {

                // ================= LOGIN =================

                case LOGIN_SUCCESS:

                    User loginUser = parseUser(data);

                    System.out.println("Parsed user = " + loginUser);
                    System.out.println("Raw login data = " + data);

                    UserSession.setCurrentUser(loginUser);

                    handleLoginSuccess(loginUser);

                    break;

                case LOGIN_FAILED:

                    showError(
                            "Login failed: "
                                    + (data.isEmpty()
                                    ? "Invalid username or password."
                                    : data)
                    );

                    break;

                // ================= REGISTER =================

                case REGISTER_SUCCESS:

                    NavigationUtils.switchScene(
                            mainStage,
                            "/fxml/login-view.fxml",
                            "Login"
                    );

                    showToast("Account created successfully!");

                    break;

                case REGISTER_FAILED:

                    showError(
                            "Registration failed: "
                                    + (data.isEmpty()
                                    ? "Username or email already exists."
                                    : data)
                    );

                    break;

                // ================= CREATE AUCTION =================

                case CREATE_SUCCESS:

                    showToast(
                            "Your auction has been listed successfully!"
                    );

                    NavigationUtils.switchScene(
                            mainStage,
                            "/fxml/HomePage.fxml",
                            "Auction Home"
                    );

                    break;

                case CREATE_FAILED:

                    showError(
                            "Failed to create auction: "
                                    + (data.isEmpty()
                                    ? "Please check your input."
                                    : data)
                    );

                    break;

                // ================= BID =================

                case BID_SUCCESS:

                    showToast("Your bid has been placed!");

                    ClientSocket.getInstance().sendList();

                    break;

                case BID_FAILED:

                    showError(
                            "Unable to place bid: "
                                    + (data.isEmpty()
                                    ? "Invalid bid amount."
                                    : data)
                    );

                    break;

                // ================= AUCTION LIST =================

                case LIST_SUCCESS:

                    List<Auction> list = parseAuctionList(data);

                    if (HomePageController.getInstance() != null) {

                        HomePageController.getInstance()
                                .updateAuctionList(list);
                    }

                    break;

                case LIST_EMPTY:

                    if (HomePageController.getInstance() != null) {

                        HomePageController.getInstance()
                                .updateAuctionList(new ArrayList<>());
                    }

                    break;

                // ================= JOIN =================

                case JOIN_SUCCESS:

                    System.out.println(
                            "Joined auction room: " + data
                    );

                    break;

                case JOIN_FAILED:

                    showError(
                            "Unable to join auction: " + data
                    );

                    break;

                // ================= ITEM LIST =================

                case ITEM_LIST_SUCCESS:

                    ObservableList<String[]> items =
                            FXCollections.observableArrayList();

                    for (String token : data.split("\\|")) {

                        String[] fields = token.split(";", -1);

                        if (fields.length >= 5) {
                            items.add(fields);
                        }
                    }

                    if (ManageProductController.getInstance() != null) {

                        ManageProductController.getInstance()
                                .updateProducts(items);
                    }

                    break;

                case ITEM_LIST_EMPTY:

                    if (ManageProductController.getInstance() != null) {

                        ManageProductController.getInstance()
                                .updateProducts(
                                        FXCollections.observableArrayList()
                                );
                    }

                    break;

                // ================= DELETE ITEM =================

                case DELETE_ITEM_SUCCESS:

                    showToast(
                            "Product deleted successfully!"
                    );

                    if (ManageProductController.getInstance() != null) {

                        ManageProductController.getInstance()
                                .handleReloadProducts();
                    }

                    break;

                case DELETE_ITEM_FAILED:

                    showError(
                            "Failed to delete product: " + data
                    );

                    break;

                // ================= UPDATE ITEM =================

                case UPDATE_ITEM_SUCCESS:

                    showToast(
                            "Product updated successfully!"
                    );

                    if (ManageProductController.getInstance() != null) {

                        ManageProductController.getInstance()
                                .handleReloadProducts();
                    }

                    break;

                case UPDATE_ITEM_FAILED:

                    showError(
                            "Failed to update product: " + data
                    );

                    break;

                // ================= FORGOT PASSWORD =================

                case FORGOT_SUCCESS:

                    NavigationUtils.switchScene(
                            mainStage,
                            "/fxml/login-view.fxml",
                            "Login"
                    );

                    showToast(
                            "Password reset successfully!"
                    );

                    break;

                case FORGOT_FAILED:

                    showError(
                            "Failed to reset password: " + data
                    );

                    break;

                // ================= USER LIST =================

                case USER_LIST_SUCCESS:

                    ObservableList<User> users =
                            FXCollections.observableArrayList();

                    for (String token : data.split("\\|")) {

                        String[] f = token.split(";", -1);

                        if (f.length >= 4) {

                            User u = new User();

                            u.setUser_id(f[0]);
                            u.setUsername(f[1]);
                            u.setEmail(f[2]);
                            u.setRole(Role.valueOf(f[3]));

                            users.add(u);
                        }
                    }

                    if (AdminController.getInstance() != null) {

                        AdminController.getInstance()
                                .updateUsers(users);
                    }

                    break;

                case USER_LIST_EMPTY:

                    break;

                // ================= DELETE USER =================

                case DELETE_USER_SUCCESS:

                    showToast(
                            "User deleted successfully!"
                    );

                    if (AdminController.getInstance() != null) {

                        AdminController.getInstance()
                                .handleReload();
                    }

                    break;

                case DELETE_USER_FAILED:

                    showError(
                            "Failed to delete user: " + data
                    );

                    break;

                // ================= AUCTION HISTORY =================

                case AUCTION_HISTORY_SUCCESS:

                    ObservableList<String[]> historyItems =
                            FXCollections.observableArrayList();

                    for (String token : data.split("\\|")) {

                        String[] fields = token.split(";", -1);

                        if (fields.length >= 5) {
                            historyItems.add(fields);
                        }
                    }

                    if (AuctionHistoryAdminController.getInstance() != null) {

                        AuctionHistoryAdminController.getInstance()
                                .updateHistory(historyItems);
                    }

                    break;

                case AUCTION_HISTORY_EMPTY:

                    if (AuctionHistoryAdminController.getInstance() != null) {

                        AuctionHistoryAdminController.getInstance()
                                .updateHistory(
                                        FXCollections.observableArrayList()
                                );
                    }

                    break;

                // ================= ALL AUCTIONS =================

                case ALL_AUCTIONS_SUCCESS:

                    ObservableList<String[]> auctions =
                            FXCollections.observableArrayList();

                    for (String token : data.split("\\|")) {

                        String[] fields = token.split(";", -1);

                        if (fields.length >= 5) {
                            auctions.add(fields);
                        }
                    }

                    if (ManageAuctionController.getInstance() != null) {

                        ManageAuctionController.getInstance()
                                .updateAuctions(auctions);
                    }

                    break;

                case ALL_AUCTIONS_EMPTY:

                    if (ManageAuctionController.getInstance() != null) {

                        ManageAuctionController.getInstance()
                                .updateAuctions(
                                        FXCollections.observableArrayList()
                                );
                    }

                    break;

                // ================= AUCTION ACTIONS =================

                case STOP_AUCTION_SUCCESS:

                    showToast(
                            "Auction stopped successfully!"
                    );

                    if (ManageAuctionController.getInstance() != null) {

                        ManageAuctionController.getInstance()
                                .handleReloadAuctions();
                    }

                    break;

                case STOP_AUCTION_FAILED:

                    showError(
                            "Failed to stop auction: " + data
                    );

                    break;

                case RESUME_AUCTION_SUCCESS:

                    showToast(
                            "Auction resumed successfully!"
                    );

                    if (ManageAuctionController.getInstance() != null) {

                        ManageAuctionController.getInstance()
                                .handleReloadAuctions();
                    }

                    break;

                case RESUME_AUCTION_FAILED:

                    showError(
                            "Failed to resume auction: " + data
                    );

                    break;

                case CANCEL_AUCTION_SUCCESS:

                    showToast(
                            "Auction cancelled successfully!"
                    );

                    if (ManageAuctionController.getInstance() != null) {

                        ManageAuctionController.getInstance()
                                .handleReloadAuctions();
                    }

                    break;

                case CANCEL_AUCTION_FAILED:

                    showError(
                            "Failed to cancel auction: " + data
                    );

                    break;

                // ================= BID HISTORY =================

                case BID_HISTORY_SUCCESS:

                    List<Bid> bidHistory =
                            parseBidHistory(data);

                    if (LiveAuctionController.getInstance() != null) {

                        LiveAuctionController.getInstance()
                                .loadBidHistory(bidHistory);
                    }

                    break;

                case BID_HISTORY_EMPTY:

                    if (LiveAuctionController.getInstance() != null) {

                        LiveAuctionController.getInstance()
                                .loadBidHistory(new ArrayList<>());
                    }

                    break;

                // ================= SYSTEM =================

                case ERROR:

                    showError(
                            "System error: " + data
                    );

                    break;

                case DISCONNECTED:

                    showError(
                            "Connection lost. Please check your internet connection."
                    );

                    break;

                default:

                    System.out.println(
                            "Unhandled response type: " + type
                    );

                    break;
            }
        });
    }

    // ==================== TOAST HELPERS ====================

    private static void showToast(String message) {
        NavigationUtils.showToast(mainStage, message);
    }

    private static void showError(String message) {
        NavigationUtils.showError(message);
    }

    // ==================== LOGIN SUCCESS ====================

    private static void handleLoginSuccess(User loginUser) {
        if (loginUser == null) {
            showError("Failed to load user information.");
            return;
        }

        if (loginUser.getRole() == Role.ADMIN) {
            NavigationUtils.switchScene(
                    mainStage,
                    "/fxml/admin-view.fxml",
                    "Admin Dashboard"
            );

        } else {
            NavigationUtils.switchScene(
                    mainStage,
                    "/fxml/HomePage.fxml",
                    "Auction Dashboard"
            );

            Platform.runLater(() -> {
                if (HomePageController.getInstance() != null) {

                    HomePageController.getInstance()
                            .setUser(loginUser);
                }
            });
        }

        Platform.runLater(() ->
                showToast(
                        "Welcome back, "
                                + loginUser.getFullname()
                                + "!"
                )
        );
    }

    // ==================== PARSE BID HISTORY ====================

    private static List<Bid> parseBidHistory(String data) {

        List<Bid> history = new ArrayList<>();

        if (data == null || data.isEmpty()) return history;

        String[] bidTokens = data.split("\\|");

        for (String token : bidTokens) {

            try {

                String[] parts = token.split(";");

                if (parts.length >= 3) {

                    Bid bid = new Bid();

                    bid.setUsername(parts[0]);

                    bid.setAmount(
                            new BigDecimal(parts[1])
                    );

                    bid.setTimeString(parts[2]);

                    history.add(bid);
                }

            } catch (Exception e) {

                System.err.println(
                        "Parse bid history error: "
                                + e.getMessage()
                );
            }
        }

        return history;
    }

    // ==================== PARSE USER ====================

    private static User parseUser(String data) {

        try {

            String[] p = data.split("\\|", -1);

            if (p.length < 5) return null;

            User u = new User();

            u.setUser_id(p[0]);
            u.setFullname(p[1]);
            u.setUsername(p[2]);
            u.setEmail(p[3]);

            u.setDob(
                    p[4].isEmpty()
                            ? null
                            : p[4]
            );

            if (p.length > 5) {
                u.setRole(Role.valueOf(p[5]));
            }

            if (p.length > 6 && !p[6].isEmpty()) {

                u.setBalance(
                        new BigDecimal(p[6])
                );
            }

            return u;

        } catch (Exception e) {

            return null;
        }
    }

    // ==================== PARSE AUCTION LIST ====================

    private static List<Auction> parseAuctionList(String data) {

        List<Auction> list = new ArrayList<>();

        if (data == null || data.isEmpty()) return list;

        String[] auctionTokens = data.split("\\|");

        for (String token : auctionTokens) {

            try {

                String[] p = token.split(";", -1);

                if (p.length < 10) continue;

                Auction a = new Auction();

                a.setAuction_id(p[0]);

                Item item = new Item();

                item.setName(p[1]);

                item.setCategory(
                        Category.valueOf(
                                p[7].toUpperCase()
                        )
                );

                item.setDescription(p[8]);

                if (!p[4].equals("NO_IMAGE")
                        && p[4] != null
                        && !p[4].isEmpty()) {

                    item.setImages(
                            Collections.singletonList(p[4])
                    );
                }

                a.setItem(item);

                a.setCurrentPrice(
                        new BigDecimal(p[2])
                );

                a.setMinIncrement(
                        new BigDecimal(p[3])
                );

                a.setStartTime(
                        LocalDateTime.parse(p[5])
                );

                a.setEndTime(
                        LocalDateTime.parse(p[6])
                );

                try {

                    a.setStatus(
                            AuctionStatus.valueOf(
                                    p[9]
                                            .trim()
                                            .toUpperCase()
                            )
                    );

                } catch (Exception e) {

                    a.setStatus(
                            AuctionStatus.ACTIVE
                    );
                }

                list.add(a);

            } catch (Exception e) {

                System.err.println(
                        "Parse error for token: "
                                + token
                );
            }
        }

        return list;
    }
}
package client.network;

import client.controller.AuctionHistoryAdminController;
import client.controller.HomePageController;
import client.controller.ManageAuctionController;
import client.controller.ManageProductController;
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

public class ResponseHandler {

    private static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void handle(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) return;

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
                case LOGIN_SUCCESS:
                    User loginUser = parseUser(data);
                    UserSession.setCurrentUser(loginUser);
                    if (loginUser != null) {
                        NavigationUtils.showToast(mainStage, "Welcome back, " + loginUser.getFullname());

                        if (loginUser.getRole() == Role.ADMIN) {
                            // Admin → vào trang quản trị
                            NavigationUtils.switchScene(mainStage, "/fxml/admin-view.fxml", "Admin Dashboard");
                        } else {
                            // User thường → vào HomePage
                            NavigationUtils.switchScene(mainStage, "/fxml/HomePage.fxml", "Auction Dashboard");
                            Platform.runLater(() -> {
                                if (HomePageController.getInstance() != null) {
                                    HomePageController.getInstance().setUser(loginUser);
                                }
                            });
                        }
                    }
                    break;
                case LOGIN_FAILED:
                    NavigationUtils.showError("Login Failed: " + (data.isEmpty() ? "Invalid credentials." : data));
                    break;

                case REGISTER_SUCCESS:
                    NavigationUtils.switchScene(mainStage, "/fxml/login-view.fxml", "Login");
                    NavigationUtils.showInfo("Registration successful! You can now log in.");
                    break;
                case REGISTER_FAILED:
                    NavigationUtils.showError("Registration Failed: " + (data.isEmpty() ? "Username or email exists." : data));
                    break;

                case CREATE_SUCCESS:
                    NavigationUtils.showInfo("Success! Your auction has been listed.");

                    client.util.NavigationUtils.switchScene(
                            mainStage,
                            "/fxml/HomePage.fxml",
                            "Auction Home"
                    );
                    break;

                case CREATE_FAILED:
                    NavigationUtils.showError("Create Failed: " + (data.isEmpty() ? "Check your input or permissions." : data));
                    break;

                case BID_SUCCESS:
                    NavigationUtils.showToast(mainStage, "Bid placed successfully!");
                    ClientSocket.getInstance().sendList(); // Cập nhật giá mới nhất lên màn hình
                    break;
                case BID_FAILED:
                    NavigationUtils.showError("Bid Rejected: " + (data.isEmpty() ? "Invalid bid amount." : data));
                    break;

                case LIST_SUCCESS:
                    List<Auction> list = parseAuctionList(data);
                    if (HomePageController.getInstance() != null) {
                        HomePageController.getInstance().updateAuctionList(list);
                    }
                    break;
                case LIST_EMPTY:
                    if (HomePageController.getInstance() != null) {
                        HomePageController.getInstance().updateAuctionList(new ArrayList<>());
                    }
                    break;

                case JOIN_SUCCESS:
                    NavigationUtils.switchScene(mainStage, "/fxml/auctionRoom-view.fxml", "Login");
                    System.out.println("Joined auction room: " + data);
                    break;
                case JOIN_FAILED:
                    NavigationUtils.showError("Could not join: " + data);
                    break;
                case ITEM_LIST_SUCCESS:
                    ObservableList<String[]> items = FXCollections.observableArrayList();
                    String[] itemTokens = data.split("\\|");
                    for (String token : itemTokens) {
                        String[] fields = token.split(";", -1);
                        if (fields.length >= 5) items.add(fields);
                    }
                    if (ManageProductController.getInstance() != null)
                        ManageProductController.getInstance().updateProducts(items);
                    break;

                case ITEM_LIST_EMPTY:
                    if (ManageProductController.getInstance() != null)
                        ManageProductController.getInstance().updateProducts(FXCollections.observableArrayList());
                    break;

                case DELETE_ITEM_SUCCESS:
                    NavigationUtils.showInfo("Xóa sản phẩm thành công!");
                    if (ManageProductController.getInstance() != null)
                        ManageProductController.getInstance().handleReloadProducts();
                    break;

                case DELETE_ITEM_FAILED:
                    NavigationUtils.showError("Xóa thất bại: " + data);
                    break;

                case UPDATE_ITEM_SUCCESS:
                    NavigationUtils.showInfo("Cập nhật thành công!");
                    if (ManageProductController.getInstance() != null)
                        ManageProductController.getInstance().handleReloadProducts();
                    break;

                case UPDATE_ITEM_FAILED:
                    NavigationUtils.showError("Cập nhật thất bại: " + data);
                    break;

                case FORGOT_SUCCESS:
                    NavigationUtils.switchScene(mainStage, "/fxml/login-view.fxml", "Login");
                    NavigationUtils.showInfo("Đặt lại mật khẩu thành công!");
                    break;

                case FORGOT_FAILED:
                    NavigationUtils.showError("Thất bại: " + data);
                    break;

                case USER_LIST_SUCCESS:
                    ObservableList<User> users = FXCollections.observableArrayList();
                    String[] userTokens = data.split("\\|");
                    for (String token : userTokens) {
                        String[] f = token.split(";", -1);
                        if (f.length < 4) continue;
                        User u = new User();
                        u.setUser_id(f[0]);
                        u.setUsername(f[1]);
                        u.setEmail(f[2]);
                        u.setRole(Role.valueOf(f[3]));
                        users.add(u);
                    }
                    if (client.controller.AdminController.getInstance() != null)
                        client.controller.AdminController.getInstance().updateUsers(users);
                    break;

                case USER_LIST_EMPTY:
                    break;

                case DELETE_USER_SUCCESS:
                    NavigationUtils.showInfo("Xóa user thành công!");
                    if (client.controller.AdminController.getInstance() != null)
                        client.controller.AdminController.getInstance().handleReload();
                    break;

                case DELETE_USER_FAILED:
                    NavigationUtils.showError("Xóa user thất bại: " + data);
                    break;
                case AUCTION_HISTORY_SUCCESS:
                    ObservableList<String[]> historyItems = FXCollections.observableArrayList();
                    String[] historyTokens = data.split("\\|");
                    for (String token : historyTokens) {
                        String[] fields = token.split(";", -1);
                        if (fields.length >= 5) historyItems.add(fields);
                    }
                    if (AuctionHistoryAdminController.getInstance() != null)
                        AuctionHistoryAdminController.getInstance().updateHistory(historyItems);
                    break;

                case AUCTION_HISTORY_EMPTY:
                    if (AuctionHistoryAdminController.getInstance() != null)
                        AuctionHistoryAdminController.getInstance().updateHistory(
                                FXCollections.observableArrayList()
                        );
                    break;

                case ERROR:
                    NavigationUtils.showError("System Error: " + data);
                    break;

                case DISCONNECTED:
                    NavigationUtils.showError("Connection Lost: Please check your internet or Server status.");
                    break;
                    //MANAGEAUCTION
                case ALL_AUCTIONS_SUCCESS:
                    ObservableList<String[]> auctions = FXCollections.observableArrayList();
                    for (String token : data.split("\\|")) {
                        String[] fields = token.split(";", -1);
                        if (fields.length >= 5) auctions.add(fields);
                    }
                    if (ManageAuctionController.getInstance() != null)
                        ManageAuctionController.getInstance().updateAuctions(auctions);
                    break;

                case ALL_AUCTIONS_EMPTY:
                    if (ManageAuctionController.getInstance() != null)
                        ManageAuctionController.getInstance().updateAuctions(FXCollections.observableArrayList());
                    break;

                case STOP_AUCTION_SUCCESS:
                    NavigationUtils.showInfo("Đã dừng auction!");
                    if (ManageAuctionController.getInstance() != null)
                        ManageAuctionController.getInstance().handleReloadAuctions();
                    break;

                case STOP_AUCTION_FAILED:
                    NavigationUtils.showError("Dừng thất bại: " + data);
                    break;

                case RESUME_AUCTION_SUCCESS:
                    NavigationUtils.showInfo("Đã khôi phục auction!");
                    if (ManageAuctionController.getInstance() != null)
                        ManageAuctionController.getInstance().handleReloadAuctions();
                    break;

                case RESUME_AUCTION_FAILED:
                    NavigationUtils.showError("Khôi phục thất bại: " + data);
                    break;

                case CANCEL_AUCTION_SUCCESS:
                    NavigationUtils.showInfo("Đã hủy auction!");
                    if (ManageAuctionController.getInstance() != null)
                        ManageAuctionController.getInstance().handleReloadAuctions();
                    break;

                case CANCEL_AUCTION_FAILED:
                    NavigationUtils.showError("Hủy thất bại: " + data);
                    break;

                default:
                    System.out.println("Unhandled response type: " + type);
                    break;
            }
        });
    }

    private static void handleLoginSuccess(String data) {
        User loginUser = parseUser(data);
        if (loginUser != null) {
            NavigationUtils.showToast(mainStage, "Welcome, " + loginUser.getFullname());
            NavigationUtils.switchScene(mainStage, "/fxml/HomePage.fxml", "Auction Dashboard");

            Platform.runLater(() -> {
                if (HomePageController.getInstance() != null) {
                    HomePageController.getInstance().setUser(loginUser);
                }
            });
        }
    }

    // Các hàm parseUser và parseAuctionList giữ nguyên như bản trước của mày...
    private static User parseUser(String data) {
        try {
            String[] p = data.split("\\|", -1);
            User u = new User();
            u.setUser_id(p[0]);
            u.setFullname(p[1]);
            u.setUsername(p[2]);
            u.setEmail(p[3]);
            u.setDob(p[4].isEmpty() ? null : p[4]);
            if (p.length > 5) u.setRole(Role.valueOf(p[5]));
            if (p.length > 6 && !p[6].isEmpty()) u.setBalance(new BigDecimal(p[6]));
            return u;
        } catch (Exception e) { return null; }
    }

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
                item.setCategory(Category.valueOf(p[7].toUpperCase()));
                item.setDescription(p[8]);
                if (!p[4].equals("NO_IMAGE")) item.setImages(Collections.singletonList(p[4]));
                a.setItem(item);
                a.setCurrentPrice(new BigDecimal(p[2]));
                a.setMinIncrement(new BigDecimal(p[3]));
                a.setStartTime(LocalDateTime.parse(p[5]));
                a.setEndTime(LocalDateTime.parse(p[6]));
                try {
                    a.setStatus(
                            AuctionStatus.valueOf(
                                    p[9].trim().toUpperCase()
                            )
                    );

                } catch (Exception e) {

                    a.setStatus(AuctionStatus.ACTIVE);
                }
                list.add(a);
            } catch (Exception e) {
                System.err.println("Parse Error Token = " + token);
                System.err.println("Error: " + e.getMessage());            }
        }
        return list;
    }

}
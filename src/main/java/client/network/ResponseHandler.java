package client.network;

import client.controller.AdminController;
import client.controller.HomePageController;
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

        // Tách header (lệnh) và data
        String[] parts = rawMessage.split("\\|", 2);
        String header = parts[0];
        String data = (parts.length > 1) ? parts[1] : "";

        ResponseType type;
        try {
            type = ResponseType.valueOf(header);
        } catch (Exception e) {
            System.err.println("Unknown response: " + header);
            return;
        }

        Platform.runLater(() -> {
            switch (type) {
                case LOGIN_SUCCESS:
                    User loginUser = parseUser(data);
                    if (loginUser != null) {
                        NavigationUtils.showToast(mainStage, "Chào mừng " + loginUser.getFullname());

                        // PHÂN QUYỀN: Nếu là ADMIN thì vào Dashboard riêng
                        if (loginUser.getRole() == Role.ADMIN) {
                            NavigationUtils.switchScene(mainStage, "/fxml/admin-view.fxml", "Admin Control Panel");
                        } else {
                            NavigationUtils.switchScene(mainStage, "/fxml/HomePage.fxml", "Auction Dashboard");
                            // Đợi scene load xong rồi set user
                            Platform.runLater(() -> {
                                if (HomePageController.getInstance() != null) {
                                    HomePageController.getInstance().setUser(loginUser);
                                }
                            });
                        }
                    }
                    break;

                case USER_LIST_SUCCESS:
                    handleUserListResponse(data);
                    break;

                case DELETE_USER_SUCCESS:
                    NavigationUtils.showInfo("Đã xóa người dùng thành công!");
                    // Tự động load lại bảng sau khi xóa
                    if (AdminController.getInstance() != null) {
                        AdminController.getInstance().handleReload();
                    }
                    break;

                case USER_LIST_EMPTY:
                    if (AdminController.getInstance() != null) {
                        AdminController.getInstance().updateUsers(FXCollections.observableArrayList());
                    }
                    break;

                case LOGIN_FAILED:
                    NavigationUtils.showError("Đăng nhập thất bại: " + data);
                    break;

                case REGISTER_SUCCESS:
                    NavigationUtils.switchScene(mainStage, "/fxml/login-view.fxml", "Login");
                    NavigationUtils.showInfo("Đăng ký thành công! Mời bạn đăng nhập.");
                    break;

                case LIST_SUCCESS:
                    List<Auction> list = parseAuctionList(data);
                    if (HomePageController.getInstance() != null) {
                        HomePageController.getInstance().updateAuctionList(list);
                    }
                    break;

                case ERROR:
                    NavigationUtils.showError("Lỗi hệ thống: " + data);
                    break;

                case DISCONNECTED:
                    NavigationUtils.showError("Mất kết nối với Server!");
                    break;

                default:
                    System.out.println("Lệnh chưa xử lý: " + type);
                    break;
            }
        });
    }

    // --- LOGIC XỬ LÝ RIÊNG CHO ADMIN ---
    private static void handleUserListResponse(String data) {
        if (data == null || data.isEmpty()) return;

        ObservableList<User> userList = FXCollections.observableArrayList();
        String[] userTokens = data.split("\\|"); // Tách từng user

        for (String token : userTokens) {
            try {
                String[] p = token.split(";"); // Tách thuộc tính user
                if (p.length >= 4) {
                    User u = new User();
                    u.setUser_id(p[0]);
                    u.setUsername(p[1]);
                    u.setEmail(p[2]);
                    u.setRole(Role.valueOf(p[3].toUpperCase()));
                    userList.add(u);
                }
            } catch (Exception e) {
                System.err.println("Lỗi parse user: " + token);
            }
        }

        if (AdminController.getInstance() != null) {
            AdminController.getInstance().updateUsers(userList);
        }
    }

    // --- CÁC HÀM PARSE DỮ LIỆU ---
    private static User parseUser(String data) {
        try {
            String[] p = data.split("\\|", -1);
            User u = new User();
            u.setUser_id(p[0]);
            u.setFullname(p[1]);
            u.setUsername(p[2]);
            u.setEmail(p[3]);
            u.setDob(p[4].isEmpty() ? null : p[4]);
            if (p.length > 5) u.setRole(Role.valueOf(p[5].toUpperCase()));
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
                    a.setStatus(AuctionStatus.valueOf(p[9].trim().toUpperCase()));
                } catch (Exception e) {
                    a.setStatus(AuctionStatus.ACTIVE);
                }
                list.add(a);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
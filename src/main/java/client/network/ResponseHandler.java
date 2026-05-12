package client.network;

import client.controller.HomePageController;
import client.util.NavigationUtils;
import common.ResponseType;
import javafx.application.Platform;
import javafx.stage.Stage;
import model.Auction;
import model.AuctionStatus;
import model.Item;
import model.Category;
import model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResponseHandler {

    private static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void handle(String rawMessage) {
        ResponseType type = ResponseType.from(rawMessage);
        if (type == null) return;

        String data = rawMessage.contains("|")
                ? rawMessage.substring(rawMessage.indexOf("|") + 1) : "";

        Platform.runLater(() -> {
            switch (type) {

                case LOGIN_SUCCESS:
                    // Parse user từ response trước khi chuyển scene
                    User loginUser = parseUser(data);
                    NavigationUtils.switchScene(mainStage, "/fxml/HomePage.fxml", "Auction Dashboard");
                    // Truyền user xuống sau khi scene đã load (HomePageController.instance đã được set)
                    if (HomePageController.getInstance() != null && loginUser != null) {
                        HomePageController.getInstance().setUser(loginUser);
                    }
                    break;

                case LOGIN_FAILED:
                    NavigationUtils.showError("Login Failed: Invalid username or password.");
                    break;

                case REGISTER_SUCCESS:
                    NavigationUtils.switchScene(mainStage, "/fxml/login-view.fxml", "Login");
                    NavigationUtils.showInfo("Registration successful! You can now log in.");
                    break;

                case REGISTER_FAILED:
                    NavigationUtils.showError("Registration Failed: Username or email exists.");
                    break;

                case CREATE_SUCCESS:
                    NavigationUtils.switchScene(mainStage, "/fxml/HomePage.fxml", "Auction Dashboard");
                    NavigationUtils.showInfo("Success! Your auction has been listed.");
                    break;

                case CREATE_FAILED:
                    NavigationUtils.showError("Create Failed: " + (data.isEmpty() ? "Check your input data." : data));
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

                case ERROR:
                    NavigationUtils.showError("Server Error: " + data);
                    break;

                case DISCONNECTED:
                    NavigationUtils.showError("Connection Lost: Server is currently unavailable.");
                    break;
            }
        });
    }

    private static User parseUser(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            String[] p = data.split("\\|", -1);
            User u = new User();
            if (p.length > 0) u.setId(p[0]);
            if (p.length > 1) u.setFullname(p[1]);
            if (p.length > 2) u.setUsername(p[2]);
            if (p.length > 3) u.setEmail(p[3]);
            if (p.length > 4) u.setDob(p[4].isEmpty() ? null : p[4]);
            return u;
        } catch (Exception e) {
            System.err.println("[ResponseHandler] Failed to parse user: " + e.getMessage());
            return null;
        }
    }

    private static List<Auction> parseAuctionList(String data) {
        List<Auction> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;

        String[] tokens = data.split("\\|");
        for (String token : tokens) {
            if (token == null || token.trim().isEmpty() || token.equalsIgnoreCase("LIST_SUCCESS")) {
                continue;
            }
            try {
                String[] p = token.split(";", -1);

                if (p.length < 8) continue;

                Auction a = new Auction();
                a.setId(p[0]);

                Item item = new Item();
                item.setName(p[1]);

                item.setCategory(Category.valueOf(p[7]));
                a.setItem(item);

                a.setCurrentPrice(new BigDecimal(p[2]));

                if (!p[3].equals("NO_IMAGE")) {
                    item.setImages(java.util.Collections.singletonList(p[4]));
                }

                a.setEndTime(LocalDateTime.parse(p[6]));

                a.setMinIncrement(new BigDecimal(p[3]));

                a.setStartTime(LocalDateTime.parse(p[5]));

                list.add(a);
            } catch (Exception e) {
                System.err.println("Lỗi parse: " + e.getMessage());
            }
        }
        return list;
    }
}
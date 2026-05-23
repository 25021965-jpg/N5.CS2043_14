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

        // ================= LIVE AUCTION =================
        if (liveAuctionListener != null) {
            if (rawMessage.startsWith("UPDATE_PRICE")
                    || rawMessage.startsWith("JOIN_SUCCESS")
                    || rawMessage.startsWith("JOIN_FAILED")
                    || rawMessage.startsWith("BID_FAILED")
                    || rawMessage.startsWith("AUCTION_ENDED")
                    || rawMessage.startsWith("YOU_WON")) {

                liveAuctionListener.accept(rawMessage);
                return;
            }
        }

        // ================= BALANCE =================
        if (rawMessage.startsWith("BALANCE_UPDATE_SUCCESS")) {
            Platform.runLater(() -> {
                String[] parts = rawMessage.split("\\|");
                if (parts.length >= 2) {
                    BigDecimal newBalance = new BigDecimal(parts[1]);

                    if (AccountBalanceController.getInstance() != null) {
                        AccountBalanceController.getInstance()
                                .updateBalanceFromServer(newBalance);
                    }

                    if (LiveAuctionController.getInstance() != null) {
                        LiveAuctionController.getInstance()
                                .updateBalance(newBalance);
                    }

                    NavigationUtils.showToast(mainStage, "Balance updated successfully!");
                }
            });
            return;
        }

        if (rawMessage.startsWith("BALANCE_UPDATE_FAILED")) {
            Platform.runLater(() -> {
                String msg = rawMessage.split("\\|").length > 1
                        ? rawMessage.split("\\|")[1]
                        : "Transaction failed";
                NavigationUtils.showError(msg);
            });
            return;
        }

        // ================= TRANSACTIONS =================
        if (rawMessage.startsWith("TRANSACTIONS_LIST")) {
            Platform.runLater(() -> {
                String data = rawMessage.substring("TRANSACTIONS_LIST|".length());
                if (AccountBalanceController.getInstance() != null) {
                    AccountBalanceController.getInstance()
                            .updateTransactionList(data);
                }
            });
            return;
        }

        // ================= SPLIT RESPONSE =================
        String[] parts = rawMessage.split("\\|", 2);
        String header = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        ResponseType type;

        try {
            type = ResponseType.from(header);
            if (type == null) return;
        } catch (Exception e) {
            return;
        }

        Platform.runLater(() -> {

            switch (type) {

                // ================= LOGIN =================
                case LOGIN_SUCCESS -> {
                    User loginUser = parseUser(data);
                    UserSession.setCurrentUser(loginUser);
                    handleLoginSuccess(loginUser);
                }

                case LOGIN_FAILED ->
                        NavigationUtils.showError(
                                "Login failed: " + (data.isEmpty() ? "Invalid credentials" : data)
                        );

                // ================= REGISTER =================
                case REGISTER_SUCCESS -> {
                    NavigationUtils.switchScene(
                            mainStage,
                            "/fxml/login-view.fxml",
                            "Login"
                    );
                    NavigationUtils.showToast(mainStage, "Account created successfully!");
                }

                case REGISTER_FAILED ->
                        NavigationUtils.showError("Register failed: " + data);

                // ================= AUCTION LIST =================
                case LIST_SUCCESS -> {
                    List<Auction> list = parseAuctionList(data);
                    if (HomePageController.getInstance() != null) {
                        HomePageController.getInstance().updateAuctionList(list);
                    }
                }

                case LIST_EMPTY -> {
                    if (HomePageController.getInstance() != null) {
                        HomePageController.getInstance()
                                .updateAuctionList(new ArrayList<>());
                    }
                }

                // ================= FAVORITE (NEW) =================
                case LIST_FAVOURITES_SUCCESS -> {
                    FavouriteController ctrl = FavouriteController.getInstance();
                    if (ctrl != null) {
                        ctrl.renderFavourite(data);
                    }
                }

                case ADD_FAVOURITE_SUCCESS -> {

                    NavigationUtils.showToast(
                            mainStage,
                            "Added to favourites!"
                    );

                    FavouriteController ctrl =
                            FavouriteController.getInstance();

                    if (ctrl != null)
                        ctrl.loadFavourites();
                }

                case ADD_FAVOURITE_FAILED -> NavigationUtils.showError("Add favourite failed: " + data);

                case REMOVE_FAVOURITE_SUCCESS -> {
                    FavouriteController ctrl = FavouriteController.getInstance();
                    if (ctrl != null) {
                        ctrl.removeItemFromUI(data);
                    }
                }

                // ================= BID =================
                case BID_SUCCESS -> {
                    NavigationUtils.showToast(mainStage, "Bid placed!");
                    ClientSocket.getInstance().sendList();
                }

                case BID_FAILED ->
                        NavigationUtils.showError("Bid failed: " + data);

                // ================= ITEM =================
                case ITEM_LIST_SUCCESS -> {
                    ObservableList<String[]> items = FXCollections.observableArrayList();

                    for (String token : data.split("\\|")) {
                        String[] f = token.split(";", -1);
                        if (f.length >= 5) items.add(f);
                    }

                    if (ManageProductController.getInstance() != null) {
                        ManageProductController.getInstance().updateProducts(items);
                    }
                }

                case ITEM_LIST_EMPTY -> {
                    if (ManageProductController.getInstance() != null) {
                        ManageProductController.getInstance()
                                .updateProducts(FXCollections.observableArrayList());
                    }
                }

                default ->
                        System.out.println("Unhandled: " + type);
            }
        });
    }

    // ================= LOGIN SUCCESS =================
    private static void handleLoginSuccess(User user) {

        if (user == null) return;

        if (user.getRole() == Role.ADMIN) {
            NavigationUtils.switchScene(mainStage, "/fxml/admin-view.fxml", "Admin");
        } else {
            NavigationUtils.switchScene(mainStage, "/fxml/HomePage.fxml", "Home");

            Platform.runLater(() -> {
                if (HomePageController.getInstance() != null) {
                    HomePageController.getInstance().setUser(user);
                }
            });
        }

        NavigationUtils.showToast(mainStage,
                "Welcome " + user.getFullname());
    }

    // ================= PARSE USER =================
    private static User parseUser(String data) {
        try {
            String[] p = data.split("\\|", -1);
            if (p.length < 4) return null;

            User u = new User();
            u.setUser_id(p[0]);
            u.setFullname(p[1]);
            u.setUsername(p[2]);
            u.setEmail(p[3]);

            return u;

        } catch (Exception e) {
            return null;
        }
    }

    // ================= PARSE AUCTION =================
    private static List<Auction> parseAuctionList(String data){

        List<Auction> list=new ArrayList<>();

        for(String token:data.split("\\|")){

            try{

                String[] p=token.split(";",-1);

                if(p.length<12) continue;

                Auction a=new Auction();
                a.setAuction_id(p[0]);

                Item item=new Item();

                item.setItem_id(p[1]);          // FIX
                item.setName(p[2]);
                item.setDescription(p[9]);

                try{
                    item.setCategory(
                            Category.valueOf(
                                    p[8].trim().toUpperCase()
                            )
                    );
                }catch(Exception e){
                    item.setCategory(Category.OTHER);
                }

                if(!p[5].isBlank()){
                    item.setImages(
                            Collections.singletonList(p[5])
                    );
                }

                a.setItem(item);

                a.setCurrentPrice(
                        new BigDecimal(p[3])
                );

                a.setMinIncrement(
                        new BigDecimal(p[4])
                );

                a.setStartTime(
                        LocalDateTime.parse(p[6])
                );

                a.setEndTime(
                        LocalDateTime.parse(p[7])
                );
                a.setStatus(
                        AuctionStatus.valueOf(
                                p[10].trim().toUpperCase()
                        )
                );
                list.add(a);

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return list;
    }
}
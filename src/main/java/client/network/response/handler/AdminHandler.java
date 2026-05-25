package client.network.response.handler;

import client.controller.AdminController;
import client.controller.ManageAuctionController;
import client.controller.AuctionHistoryAdminController;
import client.network.response.parser.ItemParser;
import client.util.NavigationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import model.User;
import model.Role;

public class AdminHandler {

    // ===== USERS =====
    public static void users(String data) {

        ObservableList<User> users = FXCollections.observableArrayList();

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

        AdminController ctrl = AdminController.getInstance();
        if (ctrl != null) ctrl.updateUsers(users);
    }

    public static void deleteSuccess(Stage stage) {
        NavigationUtils.showToast(stage, "User deleted!");
        AdminController ctrl = AdminController.getInstance();
        if (ctrl != null) ctrl.handleReload();
    }

    public static void deleteFailed(String data, Stage stage) {
        NavigationUtils.showError("Delete user failed: " + data);
    }

    // ===== AUCTIONS =====
    public static void auctions(String data) {

        ObservableList<String[]> list =
                FXCollections.observableArrayList(ItemParser.parse(data, 5));

        ManageAuctionController ctrl = ManageAuctionController.getInstance();
        if (ctrl != null) ctrl.updateAuctions(list);
    }

    public static void auctionsEmpty() {
        ManageAuctionController ctrl = ManageAuctionController.getInstance();
        if (ctrl != null) ctrl.updateAuctions(FXCollections.observableArrayList());
    }

    public static void auctionActionSuccess(Stage stage) {
        NavigationUtils.showToast(stage, "Action success!");

        ManageAuctionController ctrl = ManageAuctionController.getInstance();
        if (ctrl != null) ctrl.handleReloadAuctions();
    }

    public static void stopFailed(String data, Stage stage) {
        NavigationUtils.showError("Stop failed: " + data);
    }

    public static void resumeFailed(String data, Stage stage) {
        NavigationUtils.showError("Resume failed: " + data);
    }

    public static void cancelFailed(String data, Stage stage) {
        NavigationUtils.showError("Cancel failed: " + data);
    }
}
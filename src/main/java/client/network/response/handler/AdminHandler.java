package client.network.response.handler;

import client.controller.admin.AdminController;
import client.controller.admin.AuctionHistoryAdminController;
import client.controller.admin.ManageAuctionController;
import client.controller.admin.ManageProductController;
import client.network.response.parser.ItemParser;
import client.util.NavigationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import model.Role;
import model.User;

public class AdminHandler {

    // ================= USERS =================
    public static void users(String data) {

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

        AdminController ctrl =
                AdminController.getInstance();

        if (ctrl != null) {
            ctrl.updateUsers(users);
        }
    }

    public static void usersEmpty() {

        AdminController ctrl =
                AdminController.getInstance();

        if (ctrl != null) {
            ctrl.updateUsers(
                    FXCollections.observableArrayList()
            );
        }
    }

    public static void deleteSuccess(Stage stage) {

        NavigationUtils.showToast(
                stage,
                "User deleted!"
        );

        AdminController ctrl =
                AdminController.getInstance();

        if (ctrl != null) {
            ctrl.handleReload();
        }
    }

    public static void deleteFailed(String data, Stage stage) {

        NavigationUtils.showError(
                "Delete user failed: " + data
        );
    }

    // ================= UPDATE USER ROLE =================
    public static void updateRoleSuccess(Stage stage) {

        NavigationUtils.showToast(
                stage,
                "Role updated!"
        );

        AdminController ctrl =
                AdminController.getInstance();

        if (ctrl != null) {
            ctrl.handleReload();
        }
    }

    public static void updateRoleFailed(String data, Stage stage) {

        NavigationUtils.showError(
                "Update role failed: " + data
        );
    }

    // ================= MANAGE AUCTIONS =================
    public static void auctions(String data) {

        ObservableList<String[]> list =
                FXCollections.observableArrayList(
                        ItemParser.parse(data, 5)
                );

        ManageAuctionController ctrl =
                ManageAuctionController.getInstance();

        if (ctrl != null) {
            ctrl.updateAuctions(list);
        }
    }

    public static void auctionsEmpty() {

        ManageAuctionController ctrl =
                ManageAuctionController.getInstance();

        if (ctrl != null) {
            ctrl.updateAuctions(
                    FXCollections.observableArrayList()
            );
        }
    }

    public static void auctionActionSuccess(Stage stage) {

        NavigationUtils.showToast(
                stage,
                "Action success!"
        );

        ManageAuctionController ctrl =
                ManageAuctionController.getInstance();

        if (ctrl != null) {
            ctrl.handleReloadAuctions();
        }
    }

    public static void stopFailed(String data, Stage stage) {

        NavigationUtils.showError(
                "Stop failed: " + data
        );
    }

    public static void resumeFailed(String data, Stage stage) {

        NavigationUtils.showError(
                "Resume failed: " + data
        );
    }

    public static void cancelFailed(String data, Stage stage) {

        NavigationUtils.showError(
                "Cancel failed: " + data
        );
    }

    // ================= PENDING AUCTIONS =================
    public static void pendingAuctions(String data) {

        ObservableList<String[]> items =
                FXCollections.observableArrayList();

        if (data == null || data.isEmpty()) {
            return;
        }

        for (String token : data.split("\\|")) {

            String[] fields =
                    token.split(";", -1);

            if (fields.length >= 5) {
                items.add(fields);
            }
        }

        if (ManageProductController.getInstance() != null) {

            ManageProductController
                    .getInstance()
                    .updateProducts(items);
        }
    }

    public static void pendingAuctionsEmpty() {

        if (ManageProductController.getInstance() != null) {

            ManageProductController
                    .getInstance()
                    .updateProducts(
                            FXCollections.observableArrayList()
                    );
        }
    }

    public static void approveSuccess(Stage stage) {

        NavigationUtils.showToast(
                stage,
                "Auction approved successfully!"
        );

        if (ManageProductController.getInstance() != null) {

            ManageProductController
                    .getInstance()
                    .handleReloadProducts();
        }
    }

    public static void approveFailed(String data, Stage stage) {

        NavigationUtils.showError(
                "Approve failed: " + data
        );
    }

    // ================= AUCTION HISTORY =================
    public static void auctionHistory(String data) {

        AuctionHistoryAdminController ctrl =
                AuctionHistoryAdminController.getInstance();

        if (ctrl != null) {
            ctrl.loadHistory(data);
        }
    }

    public static void auctionHistoryEmpty() {

        AuctionHistoryAdminController ctrl =
                AuctionHistoryAdminController.getInstance();

        if (ctrl != null) {
            ctrl.loadHistory("");
        }
    }
}
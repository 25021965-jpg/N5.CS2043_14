package client.network.response.handler;

import client.controller.AdminManageUserController;
import client.controller.AdminAuctionHistoryController;
import client.controller.AdminManageAuctionController;
import client.controller.AdminManageProductController;
import client.manager.ControllerRegistry;
import client.network.response.parser.ItemParser;
import client.util.AlertUtils;
import client.util.NavigationUtils;
import client.util.ToastUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import model.Entity.User.Role;
import model.Entity.User.User;
import model.Factory.UserFactory;

public class AdminHandler {

    // ================= USERS =================
    public static void users(String data) {
        ObservableList<User> users = FXCollections.observableArrayList();

        for (String token : data.split("\\|")) {
            String[] f = token.split(";", -1);

            if (f.length >= 4) {
                User u = UserFactory.createFromRole(f[3]);
                u.setUser_id(f[0]);
                u.setUsername(f[1]);
                u.setEmail(f[2]);
                u.setRole(Role.valueOf(f[3]));
                users.add(u);
            }
        }
        AdminManageUserController ctrl = ControllerRegistry.get(AdminManageUserController.class);
        if (ctrl != null) {
            ctrl.updateUsers(users);
        }
    }

    public static void usersEmpty() {
        AdminManageUserController ctrl = ControllerRegistry.get(AdminManageUserController.class);
        if (ctrl != null) {
            ctrl.updateUsers(FXCollections.observableArrayList());
        }
    }

    public static void deleteSuccess(Stage stage) {
        ToastUtils.show(stage, "User deleted!");
        AdminManageUserController ctrl = ControllerRegistry.get(AdminManageUserController.class);
        if (ctrl != null) {
            ctrl.handleReload();
        }
    }

    public static void deleteFailed(String data) {
        AlertUtils.error("Delete user failed: " + data);
    }

    // ================= UPDATE USER ROLE =================
    public static void updateRoleSuccess(Stage stage) {
        ToastUtils.show(stage, "Role updated!");
        AdminManageUserController ctrl = ControllerRegistry.get(AdminManageUserController.class);
        if (ctrl != null) {
            ctrl.handleReload();
        }
    }

    public static void updateRoleFailed(String data) {
        AlertUtils.error("Update role failed: " + data);
    }

    // ================= MANAGE AUCTIONS =================
    public static void auctions(String data) {
        ObservableList<String[]> list =
                FXCollections.observableArrayList(
                        ItemParser.parse(data, 5)
                );

        AdminManageAuctionController ctrl =
                ControllerRegistry.get(AdminManageAuctionController.class);
        if (ctrl != null) {
            ctrl.updateAuctions(list);
        }
    }

    public static void auctionsEmpty() {
        AdminManageAuctionController ctrl = ControllerRegistry.get(AdminManageAuctionController.class);

        if (ctrl != null) {
            ctrl.updateAuctions(
                    FXCollections.observableArrayList()
            );
        }
    }

    public static void auctionActionSuccess(Stage stage) {
        ToastUtils.show(stage, "Action success!");
        AdminManageAuctionController ctrl = ControllerRegistry.get(AdminManageAuctionController.class);
        if (ctrl != null) {
            ctrl.handleReloadAuctions();
        }
    }

    public static void stopFailed(String data) {
        AlertUtils.error("Stop failed: " + data);
    }

    public static void resumeFailed(String data) {
        AlertUtils.error("Resume failed: " + data);
    }

    public static void cancelFailed(String data) {
        AlertUtils.error("Cancel failed: " + data);
    }

    // ================= PENDING AUCTIONS =================
    public static void pendingAuctions(String data) {
        ObservableList<String[]> items = FXCollections.observableArrayList();
        if (data == null || data.isEmpty()) {
            return;
        }

        for (String token : data.split("\\|")) {
            String[] fields = token.split(";", -1);
            if (fields.length >= 5) {
                items.add(fields);
            }
        }
        if (ControllerRegistry.get(AdminManageProductController.class)!= null) {
            ControllerRegistry.get(AdminManageProductController.class).updateProducts(items);
        }
    }

    public static void pendingAuctionsEmpty() {
        if (ControllerRegistry.get(AdminManageProductController.class)!= null) {
            ControllerRegistry.get(AdminManageProductController.class)
                    .updateProducts(
                            FXCollections.observableArrayList()
                    );
        }
    }

    public static void approveSuccess(Stage stage) {
        ToastUtils.show(stage, "Auction approved successfully!");
        if (ControllerRegistry.get(AdminManageProductController.class) != null) {
            ControllerRegistry.get(AdminManageProductController.class)
                    .handleReloadProducts();
        }
        try {
            NavigationUtils.switchScene(stage, "/fxml/adminManageProduct-view.fxml", "Manage Products");
        } catch (Exception e) {
            // ignore if navigation fails
        }
    }

    public static void approveFailed(String data) {
        AlertUtils.error("Approve failed: " + data);
    }

    // ================= AUCTION HISTORY =================
    public static void auctionHistory(String data) {
        AdminAuctionHistoryController ctrl = ControllerRegistry.get(AdminAuctionHistoryController.class);
        if (ctrl != null) {
            ctrl.loadHistory(data);
        }
    }

    public static void auctionHistoryEmpty() {
        AdminAuctionHistoryController ctrl = ControllerRegistry.get(AdminAuctionHistoryController.class);
        if (ctrl != null) {
            ctrl.loadHistory("");
        }
    }
}
package client.controller.admin;

import client.manager.ControllerRegistry;
import client.network.ClientSocket;
import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.User;

public class ManageUserController {
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> userIdCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TextField searchField;

    private Stage getStage() {return NavigationUtils.getCurrentStage();}

    @FXML
    public void initialize() {
        ControllerRegistry.register(
                ManageUserController.class,
                this
        );
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        userTable.setItems(userList);

        searchField.textProperty().addListener((obs, old, newVal) -> filterUsers(newVal));
        handleReload();

        //style
        userIdCol.prefWidthProperty().bind(
                userTable.widthProperty().multiply(0.10));

        usernameCol.prefWidthProperty().bind(
                userTable.widthProperty().multiply(0.25));

        emailCol.prefWidthProperty().bind(
                userTable.widthProperty().multiply(0.40));

        roleCol.prefWidthProperty().bind(
                userTable.widthProperty().multiply(0.20));
    }

    @FXML
    public void handleReload() {
        if (ClientSocket.getInstance() != null) ClientSocket.getInstance().sendRequest("LIST_USERS");
    }

    @FXML
    public void handleManageUsers() {
        // Đang ở đây rồi thì chỉ cần reload dữ liệu
        handleReload();
    }

    private User getSelectedUser(String errorMessage) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NavigationUtils.showError(errorMessage);
            return null;
        }
        return selected;
    }

    @FXML
    public void handleManageProducts() {
        NavigationUtils.switchScene(getStage(), "/fxml/manageProduct-view.fxml", "Manage Products");
    }

    @FXML
    public void handleAuctionHistory() {
        NavigationUtils.switchScene(getStage(), "/fxml/auctionHAdmin-view.fxml", "Auction History");
    }

    @FXML
    public void handleManageAuctions() {
        NavigationUtils.switchScene(getStage(), "/fxml/manageAuction-view.fxml", "Manage Auctions");
    }

    @FXML
    public void handleLogout() {
        if (ClientSocket.getInstance() != null) ClientSocket.getInstance().sendLogout();
        NavigationUtils.switchScene(getStage(), "/fxml/login-view.fxml", "Login");
    }

    @FXML
    private void handleDeleteUser() {
        User selected = getSelectedUser("Please select a user to delete!");
        if (selected == null) {
            return;
        }
        if (NavigationUtils.showConfirm(
                "Delete User",
                "Delete user " + selected.getUsername() + "?"
        )) {
            ClientSocket.getInstance()
                    .sendRequest(
                            "DELETE_USER|"
                                    + selected.getUser_id()
                    );
        }
    }

    public void updateUsers(ObservableList<User> users) {
        Platform.runLater(() -> userList.setAll(users));
    }

    private void filterUsers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            userTable.setItems(userList);
            return;
        }

        String search = keyword.toLowerCase();
        userTable.setItems(
                userList.filtered(u ->
                        u.getUsername().toLowerCase().contains(search)
                                || u.getEmail().toLowerCase().contains(search)
                                || u.getRole().name().toLowerCase().contains(search)
                )
        );
    }

    @FXML
    private void handleUpdateUser() {
        User selected = getSelectedUser("Select user to update!");
        if (selected == null) {
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update User Role");
        dialog.setHeaderText("User: " + selected.getUsername());

        ChoiceBox<String> roleChoice = new ChoiceBox<>();
        roleChoice.getItems().addAll("BIDDER", "SELLER", "ADMIN");
        roleChoice.setValue(selected.getRole().name());

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10,
                new Label("Select new role:"), roleChoice
        );
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> bt == ButtonType.OK ? roleChoice.getValue() : null);

        dialog.showAndWait().ifPresent(newRole -> {
            ClientSocket.getInstance().sendRequest(
                    "UPDATE_USER_ROLE|" + selected.getUser_id() + "|" + newRole
            );
        });
    }
}
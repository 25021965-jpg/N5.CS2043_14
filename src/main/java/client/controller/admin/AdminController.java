package client.controller.admin;

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

public class AdminController {
    private static AdminController instance;
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> userIdCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TextField searchField;

    public AdminController() { instance = this; }
    public static AdminController getInstance() { return instance; }

    @FXML
    public void initialize() {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        userTable.setItems(userList);

        searchField.textProperty().addListener((obs, old, newVal) -> filterUsers(newVal));
        handleReload();
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

    @FXML
    public void handleManageProducts() {
        Stage stage = (Stage) userTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/manageProduct-view.fxml", "Manage Products");
    }

    @FXML
    public void handleAuctionHistory() {
        Stage stage = (Stage) userTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/auctionHAdmin-view.fxml", "Auction History");
    }

    @FXML
    public void handleManageAuctions() {
        Stage stage = (Stage) userTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/manageAuction-view.fxml", "Manage Auctions");
    }

    @FXML
    public void handleLogout() {
        if (ClientSocket.getInstance() != null) ClientSocket.getInstance().sendLogout();
        NavigationUtils.switchScene((Stage) userTable.getScene().getWindow(), "/fxml/login-view.fxml", "Login");
    }

    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Xóa " + selected.getUsername() + "?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) ClientSocket.getInstance().sendRequest("DELETE_USER|" + selected.getUser_id());
            });
        }
    }

    public void updateUsers(ObservableList<User> users) {
        Platform.runLater(() -> userList.setAll(users));
    }

    private void filterUsers(String keyword) {
        if (keyword == null || keyword.isEmpty()) { userTable.setItems(userList); return; }
        userTable.setItems(userList.filtered(u -> u.getUsername().toLowerCase().contains(keyword.toLowerCase())));
    }

    @FXML
    private void handleUpdateUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NavigationUtils.showError("Select user to update!");
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
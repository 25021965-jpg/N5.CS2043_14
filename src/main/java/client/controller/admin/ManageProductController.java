package client.controller.admin;

import client.network.ClientSocket;
import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ManageProductController {
    private static ManageProductController instance;

    // String[]: [0]auctionId/itemId, [1]name, [2]seller, [3]price/category, [4]status
    private ObservableList<String[]> productList = FXCollections.observableArrayList();

    // Đang hiển thị pending hay all products
    private boolean showingPending = false;

    @FXML private TableView<String[]> productTable;
    @FXML private TableColumn<String[], String> productIdCol;
    @FXML private TableColumn<String[], String> productNameCol;
    @FXML private TableColumn<String[], String> categoryCol;
    @FXML private TableColumn<String[], String> sellerCol;
    @FXML private TableColumn<String[], String> statusCol;
    @FXML private TextField searchProductField;
    @FXML private Button btnApprove;      // ← nút Approve trong FXML
    @FXML private Button btnViewPending;  // ← nút xem Pending trong FXML

    public ManageProductController() { instance = this; }
    public static ManageProductController getInstance() { return instance; }

    @FXML
    public void initialize() {
        productIdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));
        productNameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        categoryCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        sellerCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[4]));

        productTable.setItems(productList);
        searchProductField.textProperty().addListener((obs, old, val) -> filterProducts(val));

        handleReloadProducts();
    }

    // ================= LOAD ALL PRODUCTS =================
    @FXML
    public void handleReloadProducts() {
        showingPending = false;
        if (btnApprove != null) btnApprove.setVisible(false);
        if (ClientSocket.getInstance() != null)
            ClientSocket.getInstance().sendRequest("LIST_ITEMS");
    }

    // ================= LOAD PENDING =================
    @FXML
    public void handleViewPending() {
        showingPending = true;
        if (btnApprove != null) btnApprove.setVisible(true);
        if (ClientSocket.getInstance() != null)
            ClientSocket.getInstance().sendRequest("LIST_PENDING_AUCTIONS");
    }

    // ================= APPROVE =================
    @FXML
    public void handleApprove() {
        String[] selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NavigationUtils.showError("Please select an auction to approve!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Approve auction: " + selected[1] + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES)
                // selected[0] = auctionId
                ClientSocket.getInstance().sendRequest("APPROVE_AUCTION|" + selected[0]);
        });
    }

    // ================= DELETE =================
    @FXML
    private void handleDeleteProduct() {
        String[] selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete item: " + selected[1] + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES)
                ClientSocket.getInstance().sendRequest("DELETE_ITEM|" + selected[0]);
        });
    }

    // ================= UPDATE =================
    @FXML
    private void handleUpdateProduct() {
        String[] selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Update Product");

        TextField nameField = new TextField(selected[1]);
        TextField descField = new TextField();
        TextField catField  = new TextField(selected[2]);

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10,
                new Label("Name:"), nameField,
                new Label("Description:"), descField,
                new Label("Category:"), catField
        );
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ?
                new String[]{nameField.getText(), descField.getText(), catField.getText()} : null);

        dialog.showAndWait().ifPresent(result -> {
            String cmd = "UPDATE_ITEM|" + selected[0] + "|" + result[0] + "|" + result[1] + "|" + result[2];
            ClientSocket.getInstance().sendRequest(cmd);
        });
    }

    // ================= UPDATE UI TỪ RESPONSE =================

    // Gọi khi nhận ITEM_LIST_SUCCESS (all products)
    public void updateProducts(ObservableList<String[]> items) {
        Platform.runLater(() -> productList.setAll(items));
    }

    // Gọi khi nhận PENDING_AUCTIONS_SUCCESS
    public void showPendingList(ObservableList<String[]> items) {
        Platform.runLater(() -> {
            ObservableList<String[]> display = FXCollections.observableArrayList();
            for (String[] f : items) {
                display.add(new String[]{
                        f[0], // ID
                        f[1], // Product Name
                        f[2], // Category
                        f[3], // Seller
                        f[4]  // Status
                });
            }
            productList.setAll(display);
            productTable.refresh();
        });
    }

    // ================= FILTER =================
    private void filterProducts(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            productTable.setItems(productList);
            return;
        }
        productTable.setItems(productList.filtered(p ->
                p[1].toLowerCase().contains(keyword.toLowerCase()) ||
                        p[2].toLowerCase().contains(keyword.toLowerCase())
        ));
    }

    // ================= NAVIGATION =================
    @FXML public void handleManageUsers() {
        Stage stage = (Stage) productTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/admin-view.fxml", "Admin");
    }
    @FXML public void handleManageAuctions() {
        Stage stage = (Stage) productTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/manageAuction-view.fxml", "Manage Auctions");
    }
    @FXML public void handleAuctionHistory() {
        Stage stage = (Stage) productTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/auctionHAdmin-view.fxml", "Auction History");
    }
    @FXML public void handleLogout() {
        if (ClientSocket.getInstance() != null) ClientSocket.getInstance().sendLogout();
        NavigationUtils.switchScene((Stage) productTable.getScene().getWindow(),
                "/fxml/login-view.fxml", "Login");
    }
}
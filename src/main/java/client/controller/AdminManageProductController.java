package client.controller;

import client.manager.ControllerRegistry;
import client.network.ClientSocket;
import client.util.AlertUtils;
import client.util.NavigationUtils;
import client.controller.ItemViewController;
import client.manager.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class AdminManageProductController {
    // String[]: [0]auctionId/itemId, [1]name, [2]seller, [3]price/category, [4]status
    private final ObservableList<String[]> productList = FXCollections.observableArrayList();

    // Đang hiển thị pending hay all products
    private boolean showingPending = false;

    @FXML private TableView<String[]> productTable;
    @FXML private TableColumn<String[], String> productIdCol;
    @FXML private TableColumn<String[], String> productNameCol;
    @FXML private TableColumn<String[], String> categoryCol;
    @FXML private TableColumn<String[], String> sellerCol;
    @FXML private TableColumn<String[], String> statusCol;
    @FXML private TextField searchProductField;

    private static AdminManageProductController instance;

    public static AdminManageProductController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        ControllerRegistry.register(
                AdminManageProductController.class,
                this
        );
        productIdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));
        productNameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        categoryCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        sellerCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[4]));

        productTable.setItems(productList);
        searchProductField.textProperty().addListener((obs, old, val) -> filterProducts(val));

        handleReloadProducts();

        //style
        productIdCol.prefWidthProperty().bind(
                productTable.widthProperty().multiply(0.12));

        productNameCol.prefWidthProperty().bind(
                productTable.widthProperty().multiply(0.30));

        categoryCol.prefWidthProperty().bind(
                productTable.widthProperty().multiply(0.20));

        sellerCol.prefWidthProperty().bind(
                productTable.widthProperty().multiply(0.20));

        statusCol.prefWidthProperty().bind(
                productTable.widthProperty().multiply(0.15));
    }

    // ================= LOAD ALL PRODUCTS =================
    @FXML
    public void handleReloadProducts() {
        showingPending = false;
        if (ClientSocket.getInstance() != null) {
            ClientSocket.getInstance().sendRequest("LIST_ITEMS");
        }
    }

    // ================= LOAD PENDING =================
    @FXML
    public void handleViewPending() {

        if (showingPending) {
            handleReloadProducts();
            return;
        }

        showingPending = true;
        ClientSocket.getInstance().sendRequest("LIST_PENDING_AUCTIONS");
    }

    // ================= OPEN SELECTED PRODUCT =================
    @FXML
    public void handleProductTableClick(MouseEvent event) {
        if (event.getClickCount() != 2) {
            return;
        }
        openSelectedProduct();
    }

    private void openSelectedProduct() {
        String[] selected = productTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        if (!showingPending) {
            AlertUtils.warning(
                "Preview only",
                "Switch to Pending Approvals to open auction details."
            );
            return;
        }

        String auctionId = selected[0];
        Stage stage = (Stage) productTable.getScene().getWindow();

        // Load auction details via server (request auction data then open detail)
        ClientSocket.getInstance().sendRequest("GET_AUCTION|" + auctionId);
        // The response handler should open the item view when data arrives
    }

    // ================= DELETE =================
    @FXML
    private void handleDeleteProduct() {

        String[] selected = productTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtils.error("Please select a product!");
            return;
        }

        boolean confirmed = AlertUtils.confirm(
                "Delete Product",
                "Delete this product: " + selected[1] + "?"
        );

        if (confirmed) {
            ClientSocket.getInstance()
                    .sendRequest("DELETE_ITEM|" + selected[0]);
        }
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
        Stage stage = NavigationUtils.getMainStage();
        NavigationUtils.switchScene(stage, "/fxml/adminManageUser-view.fxml", "Admin");
    }
    @FXML public void handleManageAuctions() {
        Stage stage = NavigationUtils.getMainStage();
        NavigationUtils.switchScene(stage, "/fxml/adminManageAuction-view.fxml", "Manage Auctions");
    }
    @FXML public void handleAuctionHistory() {
        Stage stage = NavigationUtils.getMainStage();
        NavigationUtils.switchScene(stage, "/fxml/adminAuctionHistory-view.fxml", "Auction History");
    }
    @FXML public void handleLogout() {
        if (ClientSocket.getInstance() != null) ClientSocket.getInstance().sendLogout();
        NavigationUtils.switchScene((Stage) productTable.getScene().getWindow(),
                "/fxml/login-view.fxml", "Login");
    }
}
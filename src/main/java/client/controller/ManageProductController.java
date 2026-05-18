package client.controller;

import client.network.ClientSocket;
import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ManageProductController {
    private static ManageProductController instance;

    // Dùng String[] để chứa: item_id, name, category, seller, status
    private ObservableList<String[]> productList = FXCollections.observableArrayList();

    @FXML private TableView<String[]> productTable;
    @FXML private TableColumn<String[], String> productIdCol;
    @FXML private TableColumn<String[], String> productNameCol;
    @FXML private TableColumn<String[], String> categoryCol;
    @FXML private TableColumn<String[], String> sellerCol;
    @FXML private TableColumn<String[], String> statusCol;
    @FXML private TextField searchProductField;

    public ManageProductController() { instance = this; }
    public static ManageProductController getInstance() { return instance; }

    @FXML
    public void initialize() {
        // Map từng cột theo index của String[]
        productIdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));
        productNameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        categoryCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        sellerCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[4]));

        productTable.setItems(productList);
        searchProductField.textProperty().addListener((obs, old, val) -> filterProducts(val));

        handleReloadProducts();
    }

    @FXML
    public void handleReloadProducts() {
        if (ClientSocket.getInstance() != null)
            ClientSocket.getInstance().sendRequest("LIST_ITEMS");
    }

    @FXML
    private void handleDeleteProduct() {
        String[] selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa sản phẩm: " + selected[1] + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES)
                ClientSocket.getInstance().sendRequest("DELETE_ITEM|" + selected[0]);
        });
    }

    @FXML
    private void handleUpdateProduct() {
        String[] selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Dialog nhập thông tin mới
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Update Product");

        TextField nameField = new TextField(selected[1]);
        TextField descField = new TextField(); // description không có sẵn, để trống
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

    // Được gọi từ ResponseHandler khi nhận ITEM_LIST_SUCCESS
    public void updateProducts(ObservableList<String[]> items) {
        Platform.runLater(() -> productList.setAll(items));
    }

    private void filterProducts(String keyword) {
        if (keyword == null || keyword.isEmpty()) { productTable.setItems(productList); return; }
        productTable.setItems(productList.filtered(p ->
                p[1].toLowerCase().contains(keyword.toLowerCase()) ||
                        p[2].toLowerCase().contains(keyword.toLowerCase())
        ));
    }

    // Navigation
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
        NavigationUtils.switchScene((Stage) productTable.getScene().getWindow(), "/fxml/login-view.fxml", "Login");
    }
}
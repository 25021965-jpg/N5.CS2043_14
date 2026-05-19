package client.controller;

import client.network.ClientSocket;
import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AuctionHistoryAdminController {

    private static AuctionHistoryAdminController instance;

    private ObservableList<String[]> historyList = FXCollections.observableArrayList();

    @FXML private TableView<String[]> auctionHistoryTable;
    @FXML private TableColumn<String[], String> auctionIdCol;
    @FXML private TableColumn<String[], String> itemNameCol;
    @FXML private TableColumn<String[], String> winnerCol;
    @FXML private TableColumn<String[], String> finalBidCol;
    @FXML private TableColumn<String[], String> endDateCol;
    @FXML private TextField searchAuctionField;

    public AuctionHistoryAdminController() { instance = this; }
    public static AuctionHistoryAdminController getInstance() { return instance; }

    @FXML
    public void initialize() {
        auctionIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        itemNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        winnerCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        finalBidCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        endDateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));

        auctionHistoryTable.setItems(historyList);

        searchAuctionField.textProperty().addListener((obs, old, val) -> filterHistory(val));

        handleReloadAuctionHistory();
    }

    @FXML
    public void handleReloadAuctionHistory() {
        if (ClientSocket.getInstance() != null)
            ClientSocket.getInstance().sendRequest("LIST_AUCTION_HISTORY");
    }

    @FXML
    private void handleViewAuctionDetails() {
        String[] selected = auctionHistoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Auction Details");
        alert.setHeaderText("Auction: " + selected[0]);
        alert.setContentText(
                "Item: " + selected[1] + "\n" +
                        "Winner: " + selected[2] + "\n" +
                        "Final Bid: " + selected[3] + "\n" +
                        "End Date: " + selected[4] + "\n" +
                        "Status: " + selected[5]
        );
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteAuctionHistory() {
        // Auction History chỉ xem — không nên xóa thật
        // Nếu muốn xóa thì thêm lệnh DELETE_AUCTION sau
        Alert alert = new Alert(Alert.AlertType.WARNING,
                "This action will permanently delete the history and cannot be undone. Are you sure you want to continue?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                String[] selected = auctionHistoryTable.getSelectionModel().getSelectedItem();
                if (selected != null)
                    ClientSocket.getInstance().sendRequest("CANCEL_AUCTION|" + selected[0]);
            }
        });
    }

    // Được gọi từ ResponseHandler
    public void updateHistory(ObservableList<String[]> data) {
        Platform.runLater(() -> historyList.setAll(data));
    }

    private void filterHistory(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            auctionHistoryTable.setItems(historyList);
            return;
        }
        auctionHistoryTable.setItems(historyList.filtered(row ->
                row[1].toLowerCase().contains(keyword.toLowerCase()) ||
                        row[2].toLowerCase().contains(keyword.toLowerCase())
        ));
    }

    // Navigation
    @FXML public void handleManageUsers() {
        Stage stage = (Stage) auctionHistoryTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/admin-view.fxml", "Admin");
    }
    @FXML public void handleManageProducts() {
        Stage stage = (Stage) auctionHistoryTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/manageProduct-view.fxml", "Manage Products");
    }
    @FXML public void handleManageAuctions() {
        Stage stage = (Stage) auctionHistoryTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/manageAuction-view.fxml", "Manage Auctions");
    }
    @FXML public void handleLogout() {
        if (ClientSocket.getInstance() != null) ClientSocket.getInstance().sendLogout();
        NavigationUtils.switchScene(
                (Stage) auctionHistoryTable.getScene().getWindow(),
                "/fxml/login-view.fxml", "Login"
        );
    }
}
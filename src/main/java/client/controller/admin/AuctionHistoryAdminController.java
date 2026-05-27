package client.controller.admin;

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

    // Được gọi từ ResponseHandler
    public void updateHistory(ObservableList<String[]> data) {
        Platform.runLater(() -> historyList.setAll(data));
    }

    public void loadHistory(String data) {
        ObservableList<String[]> list =
                FXCollections.observableArrayList();

        if (data != null && !data.isEmpty()) {
            for (String token : data.split("\\|")) {
                String[] fields =
                        token.split(";", -1);

                if (fields.length >= 5) {
                    list.add(fields);
                }
            }
        }

        updateHistory(list);
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
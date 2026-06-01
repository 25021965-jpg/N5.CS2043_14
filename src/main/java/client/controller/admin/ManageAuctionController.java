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

public class ManageAuctionController {

    private static ManageAuctionController instance;
    private ObservableList<String[]> auctionList = FXCollections.observableArrayList();

    @FXML private TableView<String[]>       auctionTable;
    @FXML private TableColumn<String[], String> auctionIdCol;
    @FXML private TableColumn<String[], String> auctionItemCol;
    @FXML private TableColumn<String[], String> sellerCol;
    @FXML private TableColumn<String[], String> currentBidCol;
    @FXML private TableColumn<String[], String> auctionStatusCol;
    @FXML private TextField searchAuctionField;

    public ManageAuctionController() { instance = this; }
    public static ManageAuctionController getInstance() { return instance; }

    @FXML
    public void initialize() {
        auctionIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        auctionItemCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        sellerCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        currentBidCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        auctionStatusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));

        auctionTable.setItems(auctionList);
        searchAuctionField.textProperty().addListener((obs, old, val) -> filterAuctions(val));

        handleReloadAuctions();

        //style
        auctionIdCol.prefWidthProperty().bind(
                auctionTable.widthProperty().multiply(0.12));

        auctionItemCol.prefWidthProperty().bind(
                auctionTable.widthProperty().multiply(0.30));

        sellerCol.prefWidthProperty().bind(
                auctionTable.widthProperty().multiply(0.20));

        currentBidCol.prefWidthProperty().bind(
                auctionTable.widthProperty().multiply(0.18));

        auctionStatusCol.prefWidthProperty().bind(
                auctionTable.widthProperty().multiply(0.18));
    }

    @FXML
    public void handleReloadAuctions() {
        if (ClientSocket.getInstance() != null)
            ClientSocket.getInstance().sendRequest("LIST_ALL_AUCTIONS");
    }

    @FXML
    private void handleStopAuction() {
        String[] selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { NavigationUtils.showError("Chọn auction cần dừng!"); return; }
        if (selected[4].equals("ENDED") || selected[4].equals("CANCELLED")) {
            NavigationUtils.showError("Auction này đã kết thúc hoặc bị hủy!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Dừng auction: " + selected[1] + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES)
                ClientSocket.getInstance().sendRequest("STOP_AUCTION|" + selected[0]);
        });
    }

    @FXML
    private void handleResumeAuction() {
        String[] selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { NavigationUtils.showError("Chọn auction cần khôi phục!"); return; }
        if (!selected[4].equals("CANCELLED")) {
            NavigationUtils.showError("Chỉ có thể resume auction đang CANCELLED!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Khôi phục auction: " + selected[1] + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES)
                ClientSocket.getInstance().sendRequest("RESUME_AUCTION|" + selected[0]);
        });
    }

    @FXML
    private void handleDeleteAuction() {
        String[] selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { NavigationUtils.showError("Chọn auction cần hủy!"); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Hủy vĩnh viễn auction: " + selected[1] + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES)
                ClientSocket.getInstance().sendRequest("CANCEL_AUCTION|" + selected[0]);
        });
    }

    // Được gọi từ ResponseHandler
    public void updateAuctions(ObservableList<String[]> data) {
        Platform.runLater(() -> auctionList.setAll(data));
    }

    private void filterAuctions(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            auctionTable.setItems(auctionList); return;
        }
        auctionTable.setItems(auctionList.filtered(row ->
                row[1].toLowerCase().contains(keyword.toLowerCase()) ||
                        row[2].toLowerCase().contains(keyword.toLowerCase()) ||
                        row[4].toLowerCase().contains(keyword.toLowerCase())
        ));
    }

    // Navigation
    @FXML public void handleManageUsers() {
        Stage stage = (Stage) auctionTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/admin-view.fxml", "Admin");
    }
    @FXML public void handleManageProducts() {
        Stage stage = (Stage) auctionTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/manageProduct-view.fxml", "Manage Products");
    }
    @FXML public void handleAuctionHistory() {
        Stage stage = (Stage) auctionTable.getScene().getWindow();
        NavigationUtils.switchScene(stage, "/fxml/auctionHAdmin-view.fxml", "Auction History");
    }
    @FXML public void handleLogout() {
        if (ClientSocket.getInstance() != null) ClientSocket.getInstance().sendLogout();
        NavigationUtils.switchScene(
                (Stage) auctionTable.getScene().getWindow(),
                "/fxml/login-view.fxml", "Login"
        );
    }
}
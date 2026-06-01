package client.controller.admin;

import client.manager.ControllerRegistry;
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

    private final ObservableList<String[]> auctionList = FXCollections.observableArrayList();

    @FXML private TableView<String[]>       auctionTable;
    @FXML private TableColumn<String[], String> auctionIdCol;
    @FXML private TableColumn<String[], String> auctionItemCol;
    @FXML private TableColumn<String[], String> sellerCol;
    @FXML private TableColumn<String[], String> currentBidCol;
    @FXML private TableColumn<String[], String> auctionStatusCol;
    @FXML private TextField searchAuctionField;


    @FXML
    public void initialize() {
        ControllerRegistry.register(ManageAuctionController.class, this);
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

    private String[] getSelectedAuction(String errorMessage) {
        String[] selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NavigationUtils.showError(errorMessage);
            return null;
        }
        return selected;
    }

    private Stage getStage() {
        return NavigationUtils.getCurrentStage();
    }

    @FXML
    public void handleReloadAuctions() {
        if (ClientSocket.getInstance() != null)
            ClientSocket.getInstance().sendRequest("LIST_ALL_AUCTIONS");
    }

    @FXML
    private void handleStopAuction() {
        String[] selected = getSelectedAuction("Please select an auction to stop!");
        if (selected == null) return;
        if ("ENDED".equals(selected[4])
                || "CANCELLED".equals(selected[4])) {
            NavigationUtils.showError("This auction has ended or been cancelled.");
            return;
        }

        if (NavigationUtils.showConfirm(
                "Stop Auction",
                "Stop this auction: " + selected[1] + "?"
        )) {
            ClientSocket.getInstance().sendRequest("STOP_AUCTION|" + selected[0]);
        }
    }

    @FXML
    private void handleResumeAuction() {
        String[] selected = getSelectedAuction("Please select an auction to resume!");
        if (selected == null) return;
        if (!"CANCELLED".equals(selected[4])) {
            NavigationUtils.showError("Only stopped auctions can be resumed!");
            return;
        }

        if (NavigationUtils.showConfirm(
                "Resume Auction",
                "Resume this auction: " + selected[1] + "?"
        )) {
            ClientSocket.getInstance().sendRequest("RESUME_AUCTION|" + selected[0]);
        }
    }

    @FXML
    private void handleCancelAuction() {
        String[] selected = getSelectedAuction("Please select an auction to cancel!");
        if (selected == null) return;
        if (NavigationUtils.showConfirm(
                "Cancel Auction",
                "Cancel this auction: "
                        + selected[1]
                        + "? This auction will not be able to resume."
        )) {
            ClientSocket.getInstance().sendRequest("CANCEL_AUCTION|" + selected[0]);
        }
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
        NavigationUtils.switchScene(getStage(), "/fxml/manageUser-view.fxml", "Admin");
    }
    @FXML public void handleManageProducts() {
        NavigationUtils.switchScene(getStage(), "/fxml/manageProduct-view.fxml", "Manage Products");
    }
    @FXML public void handleAuctionHistory() {
        NavigationUtils.switchScene(getStage(), "/fxml/auctionHAdmin-view.fxml", "Auction History");
    }
    @FXML public void handleLogout() {
        if (ClientSocket.getInstance() != null) ClientSocket.getInstance().sendLogout();
        NavigationUtils.switchScene(
                getStage(),
                "/fxml/login-view.fxml", "Login"
        );
    }
}
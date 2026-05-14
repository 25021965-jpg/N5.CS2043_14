package client.controller;

import client.network.ClientSocket;
import client.network.ResponseHandler;
import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Auction;
import model.Category;
import model.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomePageController {

    private static HomePageController instance;
    private static final int MAX_COLUMNS = 3;

    private final List<Auction> auctionList = new ArrayList<>();

    // Filter states
    private Category selectedCategory = null;
    private String selectedStatus = null;

    @FXML private GridPane itemGrid;
    @FXML private TextField txtSearch;

    public HomePageController() {
        instance = this;
    }

    public static HomePageController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        log("Auction Dashboard Initialized");

        Platform.runLater(() -> {
            if (itemGrid != null && itemGrid.getScene() != null) {
                Stage stage = (Stage) itemGrid.getScene().getWindow();
                ResponseHandler.setMainStage(stage);
            }
        });

        ClientSocket socket = ClientSocket.getInstance();
        if (socket != null) {
            socket.sendList();
        }
    }

    public void setUser(User user) {
        if (user != null) log("Logged in as: " + user.getUsername());
    }

    // ================= DYNAMIC UPDATES (Called by ResponseHandler) =================

    public void updateAuctionList(List<Auction> auctions) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateAuctionList(auctions));
            return;
        }
        this.auctionList.clear();
        this.auctionList.addAll(auctions);
        applyFilters();
    }

    // ================= FILTER & SEARCH LOGIC =================

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFilters();
    }

    @FXML private void showAll() { selectedCategory = null; selectedStatus = null; applyFilters(); }
    @FXML private void showAccessories() { filterByCategory(Category.ACCESSORIES); }
    @FXML private void showCollectibles() { filterByCategory(Category.COLLECTIBLES); }
    @FXML private void showElectronics() { filterByCategory(Category.ELECTRONICS); }
    @FXML private void showFashion() { filterByCategory(Category.FASHION); }
    @FXML private void showHomeAppliances() { filterByCategory(Category.HOME_APPLIANCES); }
    @FXML private void showVehicles() { filterByCategory(Category.VEHICLES); }
    @FXML private void showOther() { filterByCategory(Category.OTHER); }

    @FXML private void showAllStatus() { selectedStatus = null; applyFilters(); }
    @FXML private void showActive() { selectedStatus = "ACTIVE"; applyFilters(); }
    @FXML private void showEnded() { selectedStatus = "ENDED"; applyFilters(); }
    @FXML private void showCancelled() { selectedStatus = "CANCELLED"; applyFilters(); }
    @FXML private void showUpcoming() { selectedStatus = "UPCOMING"; applyFilters(); }

    private void filterByCategory(Category category) {
        this.selectedCategory = category;
        applyFilters();
    }

    private void applyFilters() {
        if (txtSearch == null) return;
        
        String keyword = txtSearch.getText().trim().toLowerCase();
        List<Auction> filtered = auctionList.stream().filter(a -> {
            if (a == null || a.getItem() == null) return false;
            if (a.getItem().getName() == null)    return false;

            boolean matchText = keyword.isEmpty() || a.getItem().getName().toLowerCase().contains(keyword);
            boolean matchCat = selectedCategory == null || a.getItem().getCategory() == selectedCategory;
            boolean matchStat = selectedStatus == null || (a.getStatus() != null && a.getStatus().name().equalsIgnoreCase(selectedStatus));
            return matchText && matchCat && matchStat;
        }).toList();

        refreshGrid(filtered);
    }

    // ================= GRID RENDERING =================

    private void refreshGrid(List<Auction> list) {
        itemGrid.getChildren().clear();
        int col = 0;
        int row = 0;

        for (Auction auction : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/itemsCard-view.fxml"));
                Node card = loader.load();
                ItemCardController controller = loader.getController();
                controller.setData(auction);

                if (col == MAX_COLUMNS) { col = 0; row++; }
                itemGrid.add(card, col++, row);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ================= NAVIGATION =================

    @FXML
    private void handleCreateAuction(ActionEvent event) {
        NavigationUtils.switchScene(getStage(event), "/fxml/createAuction-view.fxml", "Create Auction");
    }

    @FXML
    private void openProfile(ActionEvent event) {
        NavigationUtils.switchScene(getStage(event), "/fxml/userProfile-view.fxml", "My Profile");
    }

    @FXML
    private void openHistory(ActionEvent event) {
        NavigationUtils.switchScene(getStage(event), "/fxml/auctionHistory-view.fxml", "Auction History");
    }

    @FXML
    private void openYourAuctions(ActionEvent event) {
        NavigationUtils.switchScene(getStage(event), "/fxml/yourAuctions-view.fxml", "My Auctions");
    }

    @FXML
    private void openFavourite(ActionEvent event) {
        NavigationUtils.switchScene(getStage(event), "/fxml/Favourite-view.fxml", "Favorites");
    }

    @FXML
    private void openBalance(ActionEvent event) {
        NavigationUtils.showInfo("Balance feature coming soon!");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        if (NavigationUtils.showConfirm("Logout", "Are you sure you want to logout?")) {
            NavigationUtils.switchScene(getStage(event), "/fxml/login-view.fxml", "Login");
        }
    }

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        if (NavigationUtils.showConfirm("Danger", "Permanently delete account? This cannot be undone.")) {
            NavigationUtils.switchScene(getStage(event), "/fxml/login-view.fxml", "Login");
        }
    }

    // ================= UTILS =================

    private Stage getStage(ActionEvent event) {
        if (event.getSource() instanceof MenuItem item) {
            return (Stage) item.getParentPopup().getOwnerWindow();
        }
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    private void log(String msg) {
        System.out.println("[HomePage] " + msg);
    }
}
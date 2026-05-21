package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import client.network.ResponseHandler;
import client.util.NavigationUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.Parent;
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

    private final String DEFAULT_STYLE = "-fx-background-color: #d4af37;";
    private final String ACTIVE_STYLE  = "-fx-background-color: #ffe082;";

    // Filter states
    private Category selectedCategory = null;
    private String selectedStatus = null;
    private ClientSocket client;
    private User currentUser;

    @FXML private GridPane itemGrid;
    @FXML private TextField txtSearch;
    @FXML private Button btnAll;
    @FXML private Button btnAccessories;
    @FXML private Button btnCollectibles;
    @FXML private Button btnElectronics;
    @FXML private Button btnFashion;
    @FXML private Button btnHomeAppliances;
    @FXML private Button btnVehicles;
    @FXML private Button btnOther;

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

        Platform.runLater(() -> highlight(btnAll));
    }

    public void setClient(ClientSocket client) {
        this.client = client;
    }

    public void setUser(User user) {
        this.currentUser = user;
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

    @FXML
    private void showAll() {
        highlight(btnAll);
        selectedCategory = null;
        selectedStatus = null;
        applyFilters();
    }

    @FXML private void showAccessories()   { highlight(btnAccessories);   filterByCategory(Category.ACCESSORIES);    }
    @FXML private void showCollectibles()  { highlight(btnCollectibles);  filterByCategory(Category.COLLECTIBLES);   }
    @FXML private void showElectronics()   { highlight(btnElectronics);   filterByCategory(Category.ELECTRONICS);    }
    @FXML private void showFashion()       { highlight(btnFashion);       filterByCategory(Category.FASHION);        }
    @FXML private void showHomeAppliances(){ highlight(btnHomeAppliances);filterByCategory(Category.HOME_APPLIANCES);}
    @FXML private void showVehicles()      { highlight(btnVehicles);      filterByCategory(Category.VEHICLES);       }
    @FXML private void showOther()         { highlight(btnOther);         filterByCategory(Category.OTHER);          }

    @FXML private void showAllStatus()   { selectedStatus = null;         applyFilters(); }
    @FXML private void showActive()      { selectedStatus = "ACTIVE";     applyFilters(); }
    @FXML private void showEnded()       { selectedStatus = "ENDED";      applyFilters(); }
    @FXML private void showCancelled()   { selectedStatus = "CANCELLED";  applyFilters(); }
    @FXML private void showUpcoming()    { selectedStatus = "UPCOMING";   applyFilters(); }

    private void filterByCategory(Category category) {
        this.selectedCategory = category;
        applyFilters();
    }

    private void resetCategoryStyles() {
        btnAll.setStyle(DEFAULT_STYLE);
        btnAccessories.setStyle(DEFAULT_STYLE);
        btnCollectibles.setStyle(DEFAULT_STYLE);
        btnElectronics.setStyle(DEFAULT_STYLE);
        btnFashion.setStyle(DEFAULT_STYLE);
        btnHomeAppliances.setStyle(DEFAULT_STYLE);
        btnVehicles.setStyle(DEFAULT_STYLE);
        btnOther.setStyle(DEFAULT_STYLE);
    }

    private void highlight(Button btn) {
        resetCategoryStyles();
        btn.setStyle(ACTIVE_STYLE);
    }

    private void applyFilters() {
        if (txtSearch == null) return;

        String keyword = txtSearch.getText().trim().toLowerCase();
        List<Auction> filtered = auctionList.stream().filter(a -> {
            if (a == null || a.getItem() == null) return false;
            if (a.getItem().getName() == null)    return false;

            boolean matchText = keyword.isEmpty()
                    || a.getItem().getName().toLowerCase().contains(keyword);
            boolean matchCat  = selectedCategory == null
                    || a.getItem().getCategory() == selectedCategory;
            boolean matchStat = selectedStatus == null
                    || (a.getStatus() != null && a.getStatus().name().equalsIgnoreCase(selectedStatus));

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
                controller.setClient(client);
                controller.setUser(currentUser);

                card.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 1) {
                        openItemDetail(auction);
                    }
                });

                if (col == MAX_COLUMNS) { col = 0; row++; }
                itemGrid.add(card, col++, row);
            } catch (IOException e) {
                System.err.println("Error loading card: " + e.getMessage());
            }
        }
    }

    // ================= OPEN SCREENS =================

    private void openItemDetail(Auction auction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/items-view.fxml"));
            Parent root = loader.load();

            ItemViewController controller = loader.getController();
            controller.setClient(client);
            controller.setCurrentUser(currentUser);
            controller.setAuctionData(auction);

            Stage stage = (Stage) itemGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Auction Details - " + auction.getItem().getName());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            NavigationUtils.showError("Cannot open auction details!");
        }
    }

    // Mở Live Auction - thứ tự: setClient → setUser → setAuctionData
    // setAuctionData sẽ tự quyết định khôi phục balance ảo hay init mới
    public void openLiveAuction(Auction auction) {
        log("Opening live auction for: " + auction.getItem().getName());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/liveAuction.fxml"));
            Parent root = loader.load();

            LiveAuctionController controller = loader.getController();
            controller.setClient(client);
            controller.setUser(currentUser);   // set user trước (không init balance)
            controller.setAuctionData(         // setAuctionData sẽ init/khôi phục balance
                    auction.getAuction_id(),
                    auction.getItem().getName(),
                    auction.getItem().getDescription(),
                    auction.getCurrentPrice().toString(),
                    auction.getMinIncrement().toString(),
                    auction.getFloorPrice() != null ? auction.getFloorPrice().toString() : "0",
                    auction.getEndTime().toString(),
                    auction.getItem().getImages() != null && !auction.getItem().getImages().isEmpty()
                            ? auction.getItem().getImages().get(0) : null
            );

            Stage stage = (Stage) itemGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Live Auction - " + auction.getItem().getName());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            NavigationUtils.showError("Cannot open live auction: " + e.getMessage());
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/accountBalance-view.fxml"));
            Parent root = loader.load();

            AccountBalanceController controller = loader.getController();
            controller.setClient(client);

            if (currentUser != null) {
                controller.setUser(currentUser);
            } else {
                System.err.println("currentUser is null when opening balance");
            }

            Stage stage;
            if (event.getSource() instanceof MenuItem menuItem) {
                stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
            } else {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }

            stage.setScene(new Scene(root));
            stage.setTitle("Account Balance");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        if (NavigationUtils.showConfirm("Logout", "Are you sure you want to logout?")) {
            ClientSocket socket = ClientSocket.getInstance();
            if (socket != null) {
                socket.logout();
            }
            UserSession.setCurrentUser(null);
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

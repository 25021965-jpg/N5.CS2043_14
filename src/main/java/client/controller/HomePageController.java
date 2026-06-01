package client.controller;

import client.manager.ControllerRegistry;
import client.manager.FavouriteManager;
import client.manager.UserSession;
import client.manager.ViewCache;
import client.network.ClientSocket;
import client.network.response.ResponseHandler;
import client.util.AuctionCardFactory;
import client.util.NavigationUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.Parent;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javafx.scene.layout.GridPane;

import javafx.stage.Stage;

import model.Auction;
import model.Category;
import model.User;

import java.util.*;

public class HomePageController extends BaseController {

    public List<Auction> getAuctionList() {
        return auctionList;
    }

    // ==================== DATA ====================
    private final List<Auction> auctionList = new ArrayList<>();
    private final Map<String, Parent> cardCache = new HashMap<>();
    private Category selectedCategory;
    private String selectedStatus;

    // ==================== FXML ====================
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

    // ==================== INIT ====================
    @FXML
    public void initialize() {
        ControllerRegistry.register(
                HomePageController.class,
                this
        );        Platform.runLater(() -> {
            if (itemGrid != null && itemGrid.getScene() != null) {
                ResponseHandler.setMainStage(
                        (Stage) itemGrid.getScene().getWindow()
                );
            }
            highlight(btnAll);
            txtSearch.textProperty().addListener(
                    (obs, oldVal, newVal) -> applyFilters()
            );

            Platform.runLater(() -> itemGrid.getScene()
                    .widthProperty()
                    .addListener((obs, oldVal, newVal) ->
                            applyFilters()));
        });
    }

    @Override
    public void setClient(ClientSocket client) {
        super.setClient(client);
        if (client == null) {
            return;
        }

        User user = UserSession.getCurrentUser();

        if (user != null) {
            client.sendList();
            client.sendGetFavourite(
                    user.getUser_id()
            );
        }
    }

    // ==================== DATA ====================
    public void setUser(User user) {
        UserSession.setCurrentUser(user);
    }

    public void updateAuctionList(List<Auction> auctions) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() ->
                    updateAuctionList(auctions)
            );
            return;
        }
        if (isSameData(auctionList, auctions)) {
            return;
        }
        auctionList.clear();
        auctionList.addAll(auctions);
        applyFilters();
    }

    private boolean isSameData(
            List<Auction> oldList,
            List<Auction> newList
    ) {

        if (oldList.size() != newList.size()) {
            return false;
        }

        for (int i = 0; i < newList.size(); i++) {
            Auction oldAuction = oldList.get(i);
            Auction newAuction = newList.get(i);
            if (!Objects.equals(
                    oldAuction.getAuction_id(),
                    newAuction.getAuction_id()
            )) {
                return false;
            }

            if (!Objects.equals(
                    oldAuction.getCurrentPrice(),
                    newAuction.getCurrentPrice()
            )) {
                return false;
            }

            if (oldAuction.getStatus()
                    != newAuction.getStatus()) {
                return false;
            }
        }
        return true;
    }

    // ==================== FILTER ====================

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void showAll() {
        selectedCategory = null;
        selectedStatus = null;
        highlight(btnAll);
        applyFilters();
    }

    @FXML
    private void showAccessories() {
        selectCategory(
                Category.ACCESSORIES,
                btnAccessories
        );
    }

    @FXML
    private void showCollectibles() {
        selectCategory(
                Category.COLLECTIBLES,
                btnCollectibles
        );
    }

    @FXML
    private void showElectronics() {
        selectCategory(
                Category.ELECTRONICS,
                btnElectronics
        );
    }

    @FXML
    private void showFashion() {
        selectCategory(
                Category.FASHION,
                btnFashion
        );
    }

    @FXML
    private void showHomeAppliances() {
        selectCategory(
                Category.HOME_APPLIANCES,
                btnHomeAppliances
        );
    }

    @FXML
    private void showVehicles() {
        selectCategory(
                Category.VEHICLES,
                btnVehicles
        );
    }

    @FXML
    private void showOther() {
        selectCategory(
                Category.OTHER,
                btnOther
        );
    }

    private void selectCategory(
            Category category,
            Button button
    ) {

        selectedCategory = category;
        highlight(button);
        applyFilters();
    }

    @FXML
    private void showAllStatus() {
        selectedStatus = null;
        applyFilters();
    }

    @FXML
    private void showActive() {
        selectedStatus = "ACTIVE";
        applyFilters();
    }

    @FXML
    private void showEnded() {
        selectedStatus = "ENDED";
        applyFilters();
    }

    @FXML
    private void showCancelled() {
        selectedStatus = "CANCELLED";
        applyFilters();
    }

    @FXML
    private void showUpcoming() {
        selectedStatus = "UPCOMING";
        applyFilters();
    }

    private void applyFilters() {
        String keyword =
                txtSearch.getText()
                        .trim()
                        .toLowerCase();

        List<Auction> filtered =
                auctionList.stream()
                        .filter(auction -> matchesSearch(auction, keyword))
                        .filter(this::matchesCategory)
                        .filter(this::matchesStatus)
                        .sorted(this::compareAuctions)
                        .toList();
        refreshGrid(filtered);
    }

    private boolean matchesSearch(
            Auction auction,
            String keyword
    ) {

        if (auction == null
                || auction.getItem() == null
                || auction.getItem().getName() == null) {

            return false;
        }

        return keyword.isEmpty()
                || auction.getItem()
                .getName()
                .toLowerCase()
                .contains(keyword);
    }

    private int compareAuctions(Auction a1, Auction a2) {
        boolean fav1 =
                FavouriteManager.isFavourite(
                        a1.getItem().getItem_id()
                );

        boolean fav2 =
                FavouriteManager.isFavourite(
                        a2.getItem().getItem_id()
                );

        if (fav1 != fav2) {
            return fav1 ? -1 : 1;
        }

        boolean active1 =
                a1.getStatus().name().equals("ACTIVE");

        boolean active2 =
                a2.getStatus().name().equals("ACTIVE");

        if (active1 != active2) {
            return active1 ? -1 : 1;
        }
        return 0;
    }

    private boolean matchesCategory(Auction auction) {
        return selectedCategory == null
                || auction.getItem()
                .getCategory()
                == selectedCategory;
    }

    private boolean matchesStatus(Auction auction) {
        return selectedStatus == null
                || (
                auction.getStatus() != null
                        && auction.getStatus()
                        .name()
                        .equalsIgnoreCase(
                                selectedStatus
                        )
        );
    }

    // ==================== GRID ====================
    private void refreshGrid(List<Auction> auctions) {

        itemGrid.getChildren().clear();

        double width = itemGrid.getScene() != null
                ? itemGrid.getScene().getWidth()
                : 1100;

        int columns = width >= 1400 ? 4 : 3;

        int column = 0;
        int row = 0;

        for (Auction auction : auctions) {
            Parent card = createAuctionCard(auction);
            if (card == null) {
                continue;
            }

            itemGrid.add(card, column, row);
            column++;

            if (column >= columns) {
                column = 0;
                row++;
            }
        }
    }

    private Parent createAuctionCard(Auction auction) {
        String id = auction.getAuction_id();
        if (cardCache.containsKey(id)) {
            return cardCache.get(id);
        }
        Parent card = AuctionCardFactory.createCard(auction, client);
        if (card != null) {
            cardCache.put(id, card);
        }
        return card;
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleCreateAuction(ActionEvent event) {
        navigate(
                getStage(event),
                "/fxml/createAuction-view.fxml",
                "Create Auction"
        );
    }

    @FXML
    private void openProfile(ActionEvent event) {
        NavigationUtils.setCurrentPage("PROFILE");
        navigate(
                getStage(event),
                "/fxml/userProfile-view.fxml",
                "My Profile"
        );
    }

    @FXML
    private void openHistory(ActionEvent event) {
        NavigationUtils.setCurrentPage("HISTORY");
        navigate(
                getStage(event),
                "/fxml/auctionHistory-view.fxml",
                "Auction History"
        );
    }

    @FXML
    private void openYourAuctions(ActionEvent event) {
        NavigationUtils.setCurrentPage("MY_AUCTIONS");
        navigate(
                getStage(event),
                "/fxml/myAuctions-view.fxml",
                "My Auctions"
        );
    }

    @FXML
    private void openFavourite(ActionEvent event) {
        NavigationUtils.setCurrentPage("FAVOURITE");
        navigate(
                getStage(event),
                "/fxml/Favourite-view.fxml",
                "Favourite"
        );
    }

    @FXML
    private void openBalance(ActionEvent event) {
        NavigationUtils.setCurrentPage("BALANCE");
        navigate(
                getStage(event),
                "/fxml/accountBalance-view.fxml",
                "Account Balance"
        );
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        boolean confirmed =
                NavigationUtils.showConfirm(
                        "Logout",
                        "Are you sure you want to logout?"
                );
        if (!confirmed) {return;}
        ViewCache.clear();
        UserSession.setCurrentUser(null);
        navigate(
                getStage(event),
                "/fxml/login-view.fxml",
                "Login"
        );
    }

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        boolean confirmed =
                NavigationUtils.showConfirm(
                        "Danger",
                        "Permanently delete account?"
                );
        if (!confirmed) {
            return;
        }
        navigate(
                getStage(event),
                "/fxml/login-view.fxml",
                "Login"
        );
    }

    // ==================== STYLE ====================
    private void resetCategoryStyles() {
        final String defaultStyle =
                "-fx-background-color: #d4af37;";

        btnAll.setStyle(defaultStyle);
        btnAccessories.setStyle(defaultStyle);
        btnCollectibles.setStyle(defaultStyle);
        btnElectronics.setStyle(defaultStyle);
        btnFashion.setStyle(defaultStyle);
        btnHomeAppliances.setStyle(defaultStyle);
        btnVehicles.setStyle(defaultStyle);
        btnOther.setStyle(defaultStyle);
    }

    private void highlight(Button button) {
        resetCategoryStyles();
        button.setStyle("-fx-background-color: #ffe082;");
    }

    public void refreshData() {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            return;
        }
        client.sendList();
        client.sendGetFavourite(
                user.getUser_id()
        );
    }
}
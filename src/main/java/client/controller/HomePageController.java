package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;
import client.network.response.ResponseHandler;
import client.util.NavigationUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javafx.scene.layout.GridPane;

import javafx.stage.Stage;

import model.Auction;
import model.Category;
import model.User;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomePageController
        extends BaseController {

    private static HomePageController instance;
    public static HomePageController getInstance() {
        return instance;
    }
    public List<Auction> getAuctionList() {
        return auctionList;
    }
    private static final int MAX_COLUMNS = 3;

    // ==================== DATA ====================
    private boolean initialized = false;
    private final List<Auction> auctionList =
            new ArrayList<>();
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
        instance = this;
        if (initialized) return;
        initialized = true;
        Platform.runLater(() -> {
            if (itemGrid != null && itemGrid.getScene() != null) {
                ResponseHandler.setMainStage(
                        (Stage) itemGrid.getScene().getWindow()
                );
            }
            highlight(btnAll);
            txtSearch.textProperty().addListener(
                    (obs, oldVal, newVal) -> applyFilters()
            );
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

    private void setupSearch() {

        txtSearch.textProperty().addListener(
                (obs, oldVal, newVal) ->
                        applyFilters()
        );
    }

    private void loadInitialData() {

        User user =
                UserSession.getCurrentUser();

        if (user == null) {
            return;
        }

        client.sendList();

        client.sendGetFavourite(
                user.getUser_id()
        );
    }

    // ==================== DATA ====================

    public void setUser(
            User user
    ) {

        UserSession.setCurrentUser(user);
    }

    public void updateAuctionList(
            List<Auction> auctions
    ) {

        if (!Platform.isFxApplicationThread()) {

            Platform.runLater(() ->
                    updateAuctionList(auctions)
            );

            return;
        }

        if (isSameData(
                auctionList,
                auctions
        )) {

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

            Auction oldAuction =
                    oldList.get(i);

            Auction newAuction =
                    newList.get(i);

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
    private void handleSearch(
            ActionEvent event
    ) {

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

                        .filter(auction ->
                                matchesSearch(
                                        auction,
                                        keyword
                                )
                        )

                        .filter(auction ->
                                matchesCategory(
                                        auction
                                )
                        )

                        .filter(auction ->
                                matchesStatus(
                                        auction
                                )
                        )

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

    private boolean matchesCategory(
            Auction auction
    ) {

        return selectedCategory == null
                || auction.getItem()
                .getCategory()
                == selectedCategory;
    }

    private boolean matchesStatus(
            Auction auction
    ) {

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

    private void refreshGrid(
            List<Auction> auctions
    ) {

        itemGrid.getChildren().clear();

        int column = 0;

        int row = 0;

        for (Auction auction : auctions) {

            Parent card =
                    createAuctionCard(auction);

            if (card == null) {
                continue;
            }

            itemGrid.add(
                    card,
                    column,
                    row
            );

            column++;

            if (column == MAX_COLUMNS) {

                column = 0;

                row++;
            }
        }
    }

    private Parent createAuctionCard(
            Auction auction
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/itemsCard-view.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            ItemCardController controller =
                    loader.getController();

            controller.setRoot(root);

            controller.setClient(client);

            controller.setData(auction);

            return root;

        } catch (IOException e) {

            System.err.println(
                    "[HomePage] Card load error: "
                            + e.getMessage()
            );

            return null;
        }
    }

    // ==================== LIVE AUCTION ====================

    public void openLiveAuction(
            Auction auction
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/liveAuction-view.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            LiveAuctionController controller =
                    loader.getController();
            controller.setClient(client);
            controller.setUser(
                    UserSession.getCurrentUser()
            );

            controller.setAuctionData(
                    auction.getAuction_id(),
                    auction.getItem().getName(),
                    auction.getItem().getDescription(),
                    auction.getCurrentPrice().toString(),
                    auction.getMinIncrement().toString(),
                    auction.getFloorPrice() != null
                            ? auction.getFloorPrice().toString()
                            : "0",
                    auction.getEndTime().toString(),

                    auction.getItem().getImages() != null
                            && !auction.getItem()
                            .getImages()
                            .isEmpty()

                            ? auction.getItem()
                              .getImages()
                              .get(0)

                            : null
            );

            Stage stage =
                    (Stage) itemGrid
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Live Auction - "
                            + auction.getItem()
                            .getName()
            );

            stage.show();

        } catch (Exception e) {

            System.err.println(
                    "[HomePage] openLiveAuction error: "
                            + e.getMessage()
            );

            showError(
                    "Cannot open live auction."
            );
        }
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleCreateAuction(
            ActionEvent event
    ) {

        navigate(
                getStage(event),
                "/fxml/createAuction-view.fxml",
                "Create Auction"
        );
    }

    @FXML
    private void openProfile(
            ActionEvent event
    ) {

        navigate(
                getStage(event),
                "/fxml/userProfile-view.fxml",
                "My Profile"
        );
    }

    @FXML
    private void openHistory(
            ActionEvent event
    ) {

        navigate(
                getStage(event),
                "/fxml/auctionHistory-view.fxml",
                "Auction History"
        );
    }

    @FXML
    private void openYourAuctions(
            ActionEvent event
    ) {

        navigate(
                getStage(event),
                "/fxml/myAuctions-view.fxml",
                "My Auctions"
        );
    }

    @FXML
    private void openFavourite(
            ActionEvent event
    ) {

        navigate(
                getStage(event),
                "/fxml/Favourite-view.fxml",
                "Favourite"
        );
    }

    @FXML
    private void openBalance(
            ActionEvent event
    ) {

        navigate(
                getStage(event),
                "/fxml/accountBalance-view.fxml",
                "Account Balance"
        );
    }

    @FXML
    private void handleLogout(
            ActionEvent event
    ) {

        boolean confirmed =
                NavigationUtils.showConfirm(
                        "Logout",
                        "Are you sure you want to logout?"
                );

        if (!confirmed) {
            return;
        }

        UserSession.setCurrentUser(null);

        navigate(
                getStage(event),
                "/fxml/login-view.fxml",
                "Login"
        );
    }

    @FXML
    private void handleDeleteAccount(
            ActionEvent event
    ) {

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
}

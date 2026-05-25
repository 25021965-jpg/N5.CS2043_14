package client.controller;

import client.manager.FavouriteManager;
import client.manager.UserSession;
import client.network.ClientSocket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import javafx.stage.Stage;

import model.Auction;
import model.Item;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FavouriteController implements UserDataReceiver {

    // ================= SINGLETON =================
    private static FavouriteController instance;

    public static FavouriteController getInstance() {
        return instance;
    }

    // ================= DATA =================
    private ClientSocket client;
    private User currentUser;
    private final List<Auction> originalAuctions =
            new ArrayList<>();

    // ================= UI =================
    @FXML private FlowPane favouriteListContainer;

    @FXML private Button infoBtn;
    @FXML private Button historyBtn;
    @FXML private Button createdAuctionBtn;
    @FXML private Button favoriteBtn;
    @FXML private Button balanceBtn;
    @FXML private Button logoutBtn;
    @FXML private Button deleteAccountBtn;
    @FXML private Button backHomeBtn;

    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;

    // ================= INIT =================
    @FXML
    public void initialize() {

        instance = this;

        System.out.println("Favourite Page Loaded");

        setupStatusFilter();
        setupCategoryFilter();
        setupMenuEvents();

        Platform.runLater(() -> {
            System.out.println("Loading favourites...");
            loadFavourites();
        });
    }

    // ================= SET DATA =================
    public void setClient(ClientSocket client) {
        this.client = client;
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    // ================= LOAD =================
    public void loadFavourites() {

        ClientSocket socket = ClientSocket.getInstance();

        if (socket == null) {
            System.err.println("ClientSocket is null");
            return;
        }

        socket.sendMessage("LIST_FAVOURITES");
    }

    // ================= RESPONSE ENTRY =================
    public void renderFavourite(String response) {

        Platform.runLater(() -> {

            favouriteListContainer.getChildren().clear();
            originalAuctions.clear();

            if (response == null
                    || response.equals("LIST_FAVOURITES_EMPTY")) {

                favouriteListContainer.getChildren().add(
                        new Label("No favourite items")
                );
                return;
            }

            try {

                // bỏ header
                String rawData =
                        response.substring(
                                "LIST_FAVOURITES_SUCCESS|".length()
                        );

                String[] auctions = rawData.split("\\|");

                System.out.println("Favourite count: "
                        + auctions.length);

                for (String auctionStr : auctions) {

                    System.out.println("RAW: " + auctionStr);

                    String[] data =
                            auctionStr.split(";", -1);


                    if (data.length < 12) {
                        System.out.println("SKIPPED");
                        continue;
                    }

                    Auction auction = new Auction();
                    auction.setAuction_id(data[0]);

                    Item item = new Item();
                    item.setItem_id(data[1]);
                    item.setName(data[2]);
                    item.setDescription(data[9]);

                    try {
                        item.setCategory(
                                model.Category.valueOf(data[8])
                        );
                    } catch (Exception e) {
                        item.setCategory(
                                model.Category.OTHER
                        );
                    }

                    // image
                    String imagePath = data[5];

                    List<String> imageList = new ArrayList<>();

                    if (imagePath != null && !imagePath.isBlank() && !imagePath.equals("NO_IMAGE")) {

                        String[] urls = imagePath.split(",");
                        for (String url : urls) {
                            String trimmedUrl = url.trim();
                            if (!trimmedUrl.isEmpty()) {
                                imageList.add(trimmedUrl);
                            }
                        }
                    }

                    item.setImages(imageList);

                    auction.setItem(item);

                    auction.setCurrentPrice(
                            new java.math.BigDecimal(data[3])
                    );

                    auction.setMinIncrement(
                            new java.math.BigDecimal(data[4])
                    );

                    auction.setStartTime(
                            java.time.LocalDateTime.parse(data[6])
                    );

                    auction.setEndTime(
                            java.time.LocalDateTime.parse(data[7])
                    );

                    auction.setStatus(
                            model.AuctionStatus.valueOf(
                                    data[10]
                                            .trim()
                                            .toUpperCase()
                            )
                    );

                    User seller = new User();
                    seller.setUser_id(data[11]);
                    auction.setSeller(seller);
                    originalAuctions.add(auction);

                    FXMLLoader loader =
                            new FXMLLoader(
                                    getClass().getResource(
                                            "/fxml/itemsCard-view.fxml"
                                    )
                            );

                    Parent card = loader.load();

                    ItemCardController controller =
                            loader.getController();

                    controller.setClient(
                            ClientSocket.getInstance()
                    );

                    controller.setRoot(card);

                    controller.setData(auction);

                    favouriteListContainer
                            .getChildren()
                            .add(card);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ================= REMOVE UI =================
    public void removeItemFromUI(String auctionId) {

        Platform.runLater(() -> {

            if (favouriteListContainer == null) return;

            favouriteListContainer.getChildren().removeIf(node -> {
                Object userData = node.getUserData();
                return auctionId != null && auctionId.equals(userData);
            });
        });
    }

    public void reloadFavourites() {
        loadFavourites();
    }


    // ================= FILTER =================
    private void setupStatusFilter() {

        statusFilterComboBox.getItems().addAll(
                "All",
                "ACTIVE",
                "UPCOMING",
                "ENDED",
                "CANCELLED"
        );

        statusFilterComboBox.setValue("All");

        statusFilterComboBox.setOnAction(
                e -> filterFavourite()
        );
    }

    private void setupCategoryFilter() {

        categoryFilterComboBox.getItems().addAll(
                "All",
                "ACCESSORIES",
                "COLLECTIBLES",
                "ELECTRONICS",
                "FASHION",
                "HOME_APPLIANCES",
                "VEHICLES",
                "OTHER"
        );

        categoryFilterComboBox.setValue("All");

        categoryFilterComboBox.setOnAction(
                e -> filterFavourite()
        );
    }

    private void filterFavourite() {

        if (favouriteListContainer == null)
            return;

        favouriteListContainer.getChildren().clear();

        String selectedStatus =
                statusFilterComboBox.getValue();

        String selectedCategory =
                categoryFilterComboBox.getValue();

        for (Auction auction : originalAuctions) {

            boolean statusMatch = true;
            boolean categoryMatch = true;

            // ================= STATUS =================
            if (selectedStatus != null
                    && !selectedStatus.equalsIgnoreCase("All")) {

                String auctionStatus =
                        auction.getStatus()
                                .name()
                                .trim()
                                .toUpperCase();

                statusMatch =
                        auctionStatus.equals(
                                selectedStatus.trim().toUpperCase()
                        );
            }

            // ================= CATEGORY =================
            if (selectedCategory != null
                    && !selectedCategory.equalsIgnoreCase("All")) {

                String category =
                        auction.getItem()
                                .getCategory()
                                .name();

                categoryMatch =
                        category.equalsIgnoreCase(selectedCategory);
            }

            // ================= RENDER =================
            if (statusMatch && categoryMatch) {

                try {

                    FXMLLoader loader =
                            new FXMLLoader(
                                    getClass().getResource(
                                            "/fxml/itemsCard-view.fxml"
                                    )
                            );

                    Parent card = loader.load();

                    ItemCardController controller =
                            loader.getController();

                    controller.setClient(
                            ClientSocket.getInstance()
                    );

                    controller.setRoot(card);

                    controller.setData(auction);

                    favouriteListContainer
                            .getChildren()
                            .add(card);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // ================= EMPTY =================
        if (favouriteListContainer
                .getChildren()
                .isEmpty()) {

            favouriteListContainer.getChildren().add(
                    new Label("No matching favourite auctions")
            );
        }
    }

    // ================= MENU =================
    private void setupMenuEvents() {

        infoBtn.setOnAction(e -> openPage("/fxml/userProfile-view.fxml", "Profile"));
        historyBtn.setOnAction(e -> openPage("/fxml/auctionHistory-view.fxml", "History"));
        createdAuctionBtn.setOnAction(e -> openPage("/fxml/myAuctions-view.fxml", "My Auctions"));
        favoriteBtn.setOnAction(e -> openPage("/fxml/favourite-view.fxml", "Favourite"));
        balanceBtn.setOnAction(e -> openPage("/fxml/accountBalance-view.fxml", "Balance"));
        backHomeBtn.setOnAction(e -> openPage("/fxml/HomePage.fxml", "Home"));

        logoutBtn.setOnAction(e -> handleLogout());
        deleteAccountBtn.setOnAction(e -> handleDeleteAccount());
    }

    // ================= NAV =================
    private void openPage(String fxmlPath, String title) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            passData(loader.getController());

            Stage stage = (Stage) backHomeBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void passData(Object controller) {
        if (controller instanceof UserDataReceiver c) {
            c.setClient(client);
            c.setUser(currentUser);
        }
    }

    // ================= ACCOUNT =================
    @FXML
    private void handleLogout() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Logout?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get().getText().equals("Logout")) {

            ClientSocket socket = ClientSocket.getInstance();
            if (socket != null) socket.logout();

            UserSession.setCurrentUser(null);

            openPage("/fxml/login-view.fxml", "Login");
        }
    }

    private void handleDeleteAccount() {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Delete account?");
        alert.showAndWait();
    }
}

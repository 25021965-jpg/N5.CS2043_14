package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import model.Auction;
import model.Item;
import model.User;

import java.io.IOException;
import java.util.Optional;

public class AuctionHistoryController implements UserDataReceiver {

    private ClientSocket client;

    private User currentUser;

    @FXML
    private Button infoBtn;

    @FXML
    private Button historyBtn;

    @FXML
    private Button createdAuctionBtn;

    @FXML
    private Button favoriteBtn;

    @FXML
    private Button balanceBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Button deleteAccountBtn;

    @FXML
    private Button backHomeBtn;

    @FXML
    private HBox auctionCard;

    @FXML
    private FlowPane historyContainer;

    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private ComboBox<String> categoryFilterComboBox;

    public void setClient(ClientSocket client) {
        this.client = client;
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    private static AuctionHistoryController instance;
    public static AuctionHistoryController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        System.out.println(
                "AuctionHistory Loaded"
        );

        setupStatusFilter();
        setupCategoryFilter();
        setupMenuEvents();
        Platform.runLater(() -> {
            loadHistory();
        });
    }

    public void loadHistory() {
        ClientSocket socket = ClientSocket.getInstance();
        if (socket == null) return;
        socket.sendMessage("LIST_JOINED_AUCTIONS");
    }

    private void setupStatusFilter() {
        statusFilterComboBox.getItems().addAll(
                "All",
                "Winning",
                "Losing",
                "Ended",
                "Cancelled"
        );

        statusFilterComboBox.setValue(
                "All"
        );

        statusFilterComboBox.setOnAction(
                e -> handleFilter()
        );
    }

    private void setupCategoryFilter() {
        categoryFilterComboBox.getItems().addAll(
                "All",
                "Accessories",
                "Collectibles",
                "Electronics",
                "Fashion",
                "Home Appliances",
                "Vehicles",
                "Other"
        );

        categoryFilterComboBox.setValue(
                "All"
        );

        categoryFilterComboBox.setOnAction(
                e -> handleFilter()
        );
    }

    public void renderHistory(String response) {

        Platform.runLater(() -> {

            historyContainer.getChildren().clear();

            if (response == null
                    || response.equals("LIST_JOINED_AUCTIONS_EMPTY")) {

                historyContainer.getChildren().add(
                        new Label("No joined auctions")
                );

                return;
            }

            try {

                String rawData =
                        response.substring(
                                "LIST_JOINED_AUCTIONS_SUCCESS|".length()
                        );

                String[] auctions = rawData.split("\\|");

                for (String auctionStr : auctions) {

                    String[] data =
                            auctionStr.split(";", -1);

                    if (data.length < 12) continue;

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
                        item.setCategory(model.Category.OTHER);
                    }

                    String imagePath = data[5];

                    if (imagePath != null && !imagePath.isBlank() && !imagePath.equals("NO_IMAGE")) {
                        String finalPath;

                        // Nếu là đường dẫn Cloudinary (http/https)
                        if (imagePath.startsWith("http")) {
                            finalPath = imagePath;
                        }
                        // Nếu là đường dẫn file local
                        else if (!imagePath.startsWith("file:")) {
                            finalPath = "file:" + imagePath.replace("\\", "/");
                        } else {
                            finalPath = imagePath;
                        }

                        item.setImages(java.util.Collections.singletonList(finalPath));
                    }

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
                            model.AuctionStatus.valueOf(data[10])
                    );

                    User seller = new User();
                    seller.setUser_id(data[11]);

                    auction.setSeller(seller);

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

                    historyContainer
                            .getChildren()
                            .add(card);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupMenuEvents() {

        infoBtn.setOnAction(
                e -> openPage(
                        "/fxml/userProfile-view.fxml",
                        "Profile"
                )
        );

        createdAuctionBtn.setOnAction(
                e -> openPage(
                        "/fxml/myAuctions-view.fxml",
                        "My Auctions"
                )
        );

        backHomeBtn.setOnAction(
                e -> openPage(
                        "/fxml/HomePage.fxml",
                        "Home"
                )
        );

        logoutBtn.setOnAction(
                e -> handleLogout()
        );

        deleteAccountBtn.setOnAction(
                e -> handleDeleteAccount()
        );

        historyBtn.setOnAction(
                e -> openPage(
                        "/fxml/auctionHistory-view.fxml",
                        "Auction History"
                )
        );

        favoriteBtn.setOnAction(
                e -> openPage(
                        "/fxml/Favourite-view.fxml",
                        "Favourite"

                )
        );

        balanceBtn.setOnAction(
                e -> openPage(
                        "/fxml/accountBalance-view.fxml",
                        "Account Balance"
                )
        );
    }

    private void handleFilter() {

        String selected =
                statusFilterComboBox.getValue();
        String selectedCategory =
                categoryFilterComboBox.getValue();

        System.out.println(
                "Status: "
                        + selected
        );
        System.out.println(
                "Category: "
                        + selectedCategory
        );

        /*
            TODO:
            - filter history by status
         */
    }

    @FXML
    private void handleLogout() {

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText(
                "Are you sure you want to logout?"
        );

        ButtonType logoutButton =
                new ButtonType("Logout");

        ButtonType cancelButton =
                new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        alert.getButtonTypes().setAll(
                logoutButton,
                cancelButton
        );

        Optional<ButtonType> result =
                alert.showAndWait();

        if (result.isPresent()
                && result.get() == logoutButton) {

            ClientSocket socket =
                    ClientSocket.getInstance();

            if (socket != null) {
                socket.logout();
            }

            UserSession.setCurrentUser(null);

            openPage(
                    "/fxml/login-view.fxml",
                    "Login"
            );
        }
    }

    private void handleDeleteAccount() {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Delete Account"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                "Are you sure you want to delete your account? This action cannot be undone."
        );

        ButtonType deleteButton =
                new ButtonType(
                        "Delete"
                );

        ButtonType cancelButton =
                new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        alert.getButtonTypes().setAll(
                deleteButton,
                cancelButton
        );

        Optional<ButtonType> result =
                alert.showAndWait();

        if (
                result.isPresent()
                        &&
                        result.get() == deleteButton
        ) {

            System.out.println(
                    "Account deleted"
            );

            openPage(
                    "/fxml/login-view.fxml",
                    "Login"
            );
        }
    }

    private void openPage(
            String fxmlPath,
            String title
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    fxmlPath
                            )
                    );

            Parent root =
                    loader.load();

            passData(
                    loader.getController()
            );

            Stage stage =
                    (Stage)
                            backHomeBtn
                                    .getScene()
                                    .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    title
            );

            stage.show();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.out.println("Cannot open: " + fxmlPath);
        }
    }

    private void passData(Object controller) {

        if (controller instanceof UserDataReceiver c) {

            c.setClient(client);
            c.setUser(currentUser);
        }
    }
}
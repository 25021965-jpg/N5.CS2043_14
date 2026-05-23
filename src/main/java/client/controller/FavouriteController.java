package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;

import model.User;

import java.io.IOException;
import java.util.Optional;

public class FavouriteController implements UserDataReceiver {

    // ================= SINGLETON =================
    private static FavouriteController instance;

    public FavouriteController() {
        instance = this;
    }

    public static FavouriteController getInstance() {
        return instance;
    }

    // ================= DATA =================
    private ClientSocket client;
    private User currentUser;

    // ================= UI =================
    @FXML private VBox favouriteListContainer;

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

        System.out.println("Favourite Page Loaded");

        setupStatusFilter();
        setupCategoryFilter();
        setupMenuEvents();

        loadFavourites();
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

            if (favouriteListContainer == null) return;

            favouriteListContainer.getChildren().clear();

            if (response == null || response.equals("FAVOURITES_EMPTY")) {
                System.out.println("No favourites");
                return;
            }

            String[] parts = response.split("\\|");
            if (parts.length < 2) return;

            for (int i = 1; i < parts.length; i++) {

                String[] data = parts[i].split(";");
                if (data.length < 7) continue;

                String auctionId = data[0];
                String name = data[2];
                String price = data[3];
                String image = data[5];

                String endTime = data.length > 7 ? data[7] : "";
                String category = data.length > 8 ? data[8] : "";

                HBox card = createFavouriteCard(
                        auctionId,
                        name,
                        price,
                        category,
                        endTime,
                        image
                );

                favouriteListContainer.getChildren().add(card);
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

    // ================= CARD =================
    private HBox createFavouriteCard(
            String auctionId,
            String name,
            String price,
            String category,
            String endTime,
            String imageUrl
    ) {

        HBox card = new HBox();
        card.setSpacing(20);
        card.setPrefHeight(150);
        card.setPrefWidth(730);

        card.setUserData(auctionId); // IMPORTANT for remove

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #D4AF37;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 15;"
        );

        Region img = new Region();
        img.setPrefSize(130, 120);
        img.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 10;");

        VBox info = new VBox();
        info.setSpacing(10);

        Label title = new Label(name);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label priceLbl = new Label("Current Price: " + price);
        priceLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #334155;");

        Label catLbl = new Label("Category: " + category);
        catLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #7C3AED; -fx-font-weight: bold;");

        Label endLbl = new Label("End Date: " + endTime);
        endLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748B;");

        Button removeBtn = new Button("Remove Favourite");

        removeBtn.setStyle(
                "-fx-background-color: #EF4444;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;"
        );

        removeBtn.setOnAction(e -> {

            ClientSocket.getInstance()
                    .sendMessage("REMOVE_FAVOURITE|" + auctionId);

            // KHÔNG reload ở đây nữa → ResponseHandler xử lý
        });

        info.getChildren().addAll(title, priceLbl, catLbl, endLbl, removeBtn);
        card.getChildren().addAll(img, info);

        return card;
    }

    // ================= FILTER =================
    private void setupStatusFilter() {
        statusFilterComboBox.getItems().addAll("All", "Active", "Finished", "Cancelled");
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.setOnAction(e -> filterFavourite());
    }

    private void setupCategoryFilter() {
        categoryFilterComboBox.getItems().addAll(
                "All", "Accessories", "Collectibles", "Electronics",
                "Fashion", "Home Appliances", "Vehicles", "Other"
        );
        categoryFilterComboBox.setValue("All");
        categoryFilterComboBox.setOnAction(e -> filterFavourite());
    }

    private void filterFavourite() {
        System.out.println("Filter TODO: send request to server");
    }

    // ================= MENU =================
    private void setupMenuEvents() {

        infoBtn.setOnAction(e -> openPage("/fxml/userProfile-view.fxml", "Profile"));
        historyBtn.setOnAction(e -> openPage("/fxml/auctionHistory-view.fxml", "History"));
        createdAuctionBtn.setOnAction(e -> openPage("/fxml/yourAuctions-view.fxml", "Your Auctions"));
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
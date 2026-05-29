package client.controller;

import client.network.ClientSocket;
import client.network.response.parser.AuctionParser;
import client.util.TextUtils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import model.Auction;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class FavouriteController implements UserDataReceiver {

    private ClientSocket client;
    private User currentUser;

    private final List<Auction> originalAuctions = new ArrayList<>();

    @FXML private FlowPane favouriteListContainer;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;

    // ================= SINGLETON =================
    private static FavouriteController instance;

    public static FavouriteController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        System.out.println("Favourite Page Loaded");

        setupStatusFilter();
        setupCategoryFilter();

        Platform.runLater(this::loadFavourites);
    }

    @Override
    public void setClient(ClientSocket client) {
        this.client = client;
    }

    @Override
    public void setUser(User user) {
        this.currentUser = user;
    }

    public void loadFavourites() {
        if (client == null) {
            System.err.println("ClientSocket is null");
            return;
        }

        client.sendMessage("LIST_FAVOURITES");
    }

    public void renderFavourite(String response) {
        Platform.runLater(() -> {

            favouriteListContainer.getChildren().clear();
            originalAuctions.clear();

            if (response == null || response.equals("LIST_FAVOURITES_EMPTY")) {
                favouriteListContainer.getChildren().add(new Label("No favourite items"));
                return;
            }

            try {

                String rawData =
                        response.substring(
                                "LIST_FAVOURITES_SUCCESS|".length()
                        );

                List<Auction> auctions =
                        AuctionParser.parseList(rawData);

                System.out.println(
                        "Favourite count: " + auctions.size()
                );

                originalAuctions.addAll(auctions);

                for (Auction auction : auctions) {
                    addAuctionCard(auction);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

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

    private void setupStatusFilter() {

        statusFilterComboBox.getItems().addAll(
                "All",
                TextUtils.toTitleCase("ACTIVE"),
                TextUtils.toTitleCase("UPCOMING"),
                TextUtils.toTitleCase("ENDED"),
                TextUtils.toTitleCase("CANCELLED")
        );

        statusFilterComboBox.setValue("All");

        statusFilterComboBox.setOnAction(e -> filterFavourite());
    }

    private void setupCategoryFilter() {

        categoryFilterComboBox.getItems().addAll(
                "All",
                TextUtils.toTitleCase("ACCESSORIES"),
                TextUtils.toTitleCase("COLLECTIBLES"),
                TextUtils.toTitleCase("ELECTRONICS"),
                TextUtils.toTitleCase("FASHION"),
                TextUtils.toTitleCase("HOME_APPLIANCES"),
                TextUtils.toTitleCase("VEHICLES"),
                TextUtils.toTitleCase("OTHER")
        );

        categoryFilterComboBox.setValue("All");

        categoryFilterComboBox.setOnAction(e -> filterFavourite());
    }

    private void filterFavourite() {

        if (favouriteListContainer == null) return;

        favouriteListContainer.getChildren().clear();

        String selectedStatus = statusFilterComboBox.getValue();
        String selectedCategory = categoryFilterComboBox.getValue();

        for (Auction auction : originalAuctions) {

            boolean statusMatch = true;
            boolean categoryMatch = true;

            if (selectedStatus != null && !selectedStatus.equalsIgnoreCase("All")) {

                String auctionStatus =
                        TextUtils.toTitleCase(
                                auction.getStatus().name()
                        );

                statusMatch = auctionStatus.equalsIgnoreCase(selectedStatus);
            }

            if (selectedCategory != null && !selectedCategory.equalsIgnoreCase("All")) {

                String category =
                        TextUtils.toTitleCase(
                                auction.getItem().getCategory().name()
                        );

                categoryMatch = category.equalsIgnoreCase(selectedCategory);
            }

            if (statusMatch && categoryMatch) {
                addAuctionCard(auction);
            }
        }

        if (favouriteListContainer.getChildren().isEmpty()) {
            favouriteListContainer.getChildren().add(
                    new Label("No matching favourite auctions")
            );
        }
    }

    private void addAuctionCard(Auction auction) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/itemsCard-view.fxml")
            );

            Parent card = loader.load();

            ItemCardController controller = loader.getController();

            controller.setClient(client);
            controller.setRoot(card);
            controller.setData(auction);

            favouriteListContainer.getChildren().add(card);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
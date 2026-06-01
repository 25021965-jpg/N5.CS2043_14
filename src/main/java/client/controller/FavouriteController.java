package client.controller;

import client.network.ClientSocket;
import client.network.response.parser.AuctionParser;
import client.util.AuctionCardFactory;
import client.util.TextUtils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;

import model.Auction;
import model.Category;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class FavouriteController
        extends BaseController
        implements UserDataReceiver {

    private static FavouriteController instance;

    public static FavouriteController getInstance() {
        return instance;
    }

    private static final List<String> STATUS_FILTERS = List.of(
            "All",
            "Active",
            "Upcoming",
            "Ended",
            "Cancelled"
    );

    private final List<Auction> originalAuctions =
            new ArrayList<>();
    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane favouriteListContainer;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;

    // ==================== INIT ====================
    @FXML
    public void initialize() {
        instance = this;
        System.out.println("Favourite Page Loaded");
        setupStatusFilter();
        setupCategoryFilter();
        Platform.runLater(() -> {

            favouriteListContainer.setPrefWrapLength(
                    scrollPane.getViewportBounds().getWidth() - 20
            );

            scrollPane.viewportBoundsProperty().addListener(
                    (obs, oldVal, newVal) ->
                            favouriteListContainer.setPrefWrapLength(
                                    newVal.getWidth() - 20
                            )
            );
        });
    }

    // ==================== CLIENT ====================
    @Override
    public void setClient(ClientSocket client) {

        super.setClient(client);

        if (client == null) {
            return;
        }

        client.setMessageListener(msg -> {

            if (msg.startsWith(
                    "LIST_FAVOURITES_SUCCESS"
            ) || msg.equals(
                    "LIST_FAVOURITES_EMPTY"
            )) {

                renderFavourite(msg);
            }
        });

        loadFavourites();
    }

    // ==================== USER ====================
    @Override
    public void setUser(User user) {}

    // ==================== LOAD ====================

    public void loadFavourites() {
        client.sendMessage("LIST_FAVOURITES");
    }

    // ==================== RENDER ====================

    public void renderFavourite(
            String response
    ) {

        runUI(() -> {

            favouriteListContainer
                    .getChildren()
                    .clear();

            originalAuctions.clear();

            if (response == null
                    || response.equals(
                    "LIST_FAVOURITES_EMPTY"
            )) {

                showEmptyMessage(
                        "No favourite items"
                );

                return;
            }

            try {

                String rawData =
                        response.substring(
                                "LIST_FAVOURITES_SUCCESS|"
                                        .length()
                        );

                List<Auction> auctions =
                        AuctionParser.parseList(
                                rawData
                        );

                System.out.println(
                        "Favourite count: "
                                + auctions.size()
                );

                originalAuctions.addAll(
                        auctions
                );

                refreshFavouriteList(
                        auctions
                );

            } catch (Exception e) {

                e.printStackTrace();

                showError(
                        "Failed to load favourites."
                );
            }
        });
    }

    // ==================== FILTER ====================

    private void setupStatusFilter() {

        statusFilterComboBox
                .getItems()
                .addAll(STATUS_FILTERS);

        statusFilterComboBox
                .setValue("All");

        statusFilterComboBox
                .setOnAction(
                        e -> filterFavourite()
                );
    }

    private void setupCategoryFilter() {

        categoryFilterComboBox
                .getItems()
                .add("All");

        for (Category category : Category.values()) {

            categoryFilterComboBox
                    .getItems()
                    .add(
                            TextUtils.toTitleCase(
                                    category.name()
                            )
                    );
        }

        categoryFilterComboBox
                .setValue("All");

        categoryFilterComboBox
                .setOnAction(
                        e -> filterFavourite()
                );
    }

    private void filterFavourite() {

        String selectedStatus =
                statusFilterComboBox.getValue();

        String selectedCategory =
                categoryFilterComboBox.getValue();

        List<Auction> filtered =
                originalAuctions.stream()

                        .filter(auction ->
                                matchesStatus(
                                        auction,
                                        selectedStatus
                                )
                        )

                        .filter(auction ->
                                matchesCategory(
                                        auction,
                                        selectedCategory
                                )
                        )

                        .toList();

        refreshFavouriteList(filtered);
    }

    private boolean matchesStatus(
            Auction auction,
            String selectedStatus
    ) {

        if (selectedStatus == null
                || selectedStatus.equalsIgnoreCase(
                "All"
        )) {

            return true;
        }

        return TextUtils.toTitleCase(
                auction.getStatus().name()
        ).equalsIgnoreCase(
                selectedStatus
        );
    }

    private boolean matchesCategory(
            Auction auction,
            String selectedCategory
    ) {

        if (selectedCategory == null
                || selectedCategory.equalsIgnoreCase(
                "All"
        )) {

            return true;
        }

        return TextUtils.toTitleCase(
                auction.getItem()
                        .getCategory()
                        .name()
        ).equalsIgnoreCase(
                selectedCategory
        );
    }

    // ==================== REFRESH ====================

    private void refreshFavouriteList(
            List<Auction> auctions
    ) {

        favouriteListContainer
                .getChildren()
                .clear();

        if (auctions.isEmpty()) {

            showEmptyMessage(
                    "No matching favourite auctions"
            );

            return;
        }

        for (Auction auction : auctions) {

            Parent card =
                    createAuctionCard(
                            auction
                    );

            if (card != null) {

                card.setUserData(
                        auction.getAuction_id()
                );

                favouriteListContainer
                        .getChildren()
                        .add(card);
            }
        }
    }

    public void reloadFavourites() {
        loadFavourites();
    }

    // ==================== CARD ====================

    private Parent createAuctionCard(
            Auction auction
    ) {
        return AuctionCardFactory.createCard(
                auction,
                client
        );
    }

    // ==================== EMPTY ====================

    private void showEmptyMessage(
            String message
    ) {

        favouriteListContainer
                .getChildren()
                .add(
                        new Label(message)
                );
    }
}


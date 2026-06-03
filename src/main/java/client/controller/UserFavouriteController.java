package client.controller;

import client.manager.ControllerRegistry;
import client.network.ClientSocket;
import client.network.response.parser.AuctionParser;
import client.util.AlertUtils;
import client.util.AuctionCardFactory;
import client.util.TextUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;

import model.Auction;
import model.Entity.Item.Category;

import java.util.*;

public class UserFavouriteController extends BaseController implements UserDataReceiver {

    private static final List<String> STATUS_FILTERS = List.of(
            "All", "Active", "Upcoming", "Ended", "Cancelled"
    );

    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane favouriteListContainer;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;

    // ==================== DATA SOURCE ====================
    private final List<Auction> originalAuctions = new ArrayList<>();
    private final Map<String, Parent> cardMap = new HashMap<>();
    private final Map<String, ItemCardController> controllerMap = new HashMap<>();
    private static final Logger LOGGER =
            Logger.getLogger(
                    UserFavouriteController.class.getName()
            );

    @FXML
    public void initialize() {
        System.out.println("Favourite Loaded");
        ControllerRegistry.register(UserFavouriteController.class, this);
        setupStatusFilter();
        setupCategoryFilter();


        Platform.runLater(() -> {
            favouriteListContainer.setPrefWrapLength(
                    scrollPane.getViewportBounds().getWidth() - 20
            );

            scrollPane.viewportBoundsProperty().addListener((obs, o, n) ->
                    favouriteListContainer.setPrefWrapLength(n.getWidth() - 20)
            );
        });
    }

    // ==================== CLIENT ====================
    @Override
    public void setClient(ClientSocket client) {
        super.setClient(client);
        if (client == null) return;
        loadFavourites();
    }

    // ==================== LOAD ====================
    public void loadFavourites() {
        if (client != null) {
            client.sendMessage("LIST_FAVOURITES");
        }
    }

    // ==================== RENDER ====================
    public void renderFavourite(String data) {

        runUI(() -> {

            favouriteListContainer.getChildren().clear();
            originalAuctions.clear();
            cardMap.clear();
            controllerMap.clear();

            if (data == null || data.isBlank()) {
                favouriteListContainer.getChildren()
                        .add(new Label("No favourite items"));
                return;
            }

            try {
                List<Auction> list = AuctionParser.parseList(data);
                originalAuctions.addAll(list);
                refreshFavouriteList(list);

            } catch (Exception e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Failed to load favourites",
                        e
                );
                AlertUtils.error("Failed to load favourites.");
            }
        });
    }

    // ==================== CORE RENDER ====================
    private void refreshFavouriteList(List<Auction> auctions) {
        favouriteListContainer.getChildren().clear();
        Set<String> newIds = new HashSet<>();
        for (Auction auction : auctions) {
            String id = auction.getAuction_id();
            newIds.add(id);

            Parent card = cardMap.get(id);
            ItemCardController controller = controllerMap.get(id);

            if (card == null || controller == null) {
                card = AuctionCardFactory.createCard(auction, client);

                if (card == null) {
                    continue;
                }

                controller = (ItemCardController)
                        card.getProperties().get("controller");

                if (controller == null) {
                    continue;
                }

                card.setUserData(id);

                cardMap.put(id, card);
                controllerMap.put(id, controller);
            }

            controller.updateAuction(auction);

            favouriteListContainer.getChildren().add(card);
        }

        cardMap.keySet().removeIf(id -> !newIds.contains(id));
        controllerMap.keySet().removeIf(id -> !newIds.contains(id));
    }

    // ==================== FILTER ====================
    private void setupStatusFilter() {
        statusFilterComboBox.getItems().addAll(STATUS_FILTERS);
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.setOnAction(e -> filterFavourite());
    }

    private void setupCategoryFilter() {
        categoryFilterComboBox.getItems().add("All");

        for (Category c : Category.values()) {
            categoryFilterComboBox.getItems().add(TextUtils.toTitleCase(c.name()));
        }

        categoryFilterComboBox.setValue("All");
        categoryFilterComboBox.setOnAction(e -> filterFavourite());
    }

    private void filterFavourite() {

        String status = statusFilterComboBox.getValue();
        String category = categoryFilterComboBox.getValue();

        List<Auction> filtered = originalAuctions.stream()
                .filter(a -> matchesStatus(a, status))
                .filter(a -> matchesCategory(a, category))
                .toList();

        refreshFavouriteList(filtered);
    }

    private boolean matchesStatus(Auction a, String s) {
        return s == null || s.equalsIgnoreCase("All")
                || TextUtils.toTitleCase(a.getStatus().name()).equalsIgnoreCase(s);
    }

    private boolean matchesCategory(Auction a, String c) {
        return c == null || c.equalsIgnoreCase("All")
                || TextUtils.toTitleCase(a.getItem().getCategory().name()).equalsIgnoreCase(c);
    }
}
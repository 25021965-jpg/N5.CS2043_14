package client.controller;

import client.manager.ControllerRegistry;
import client.network.ClientSocket;
import client.network.response.parser.AuctionParser;
import client.util.AlertUtils;
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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAuctionHistoryController extends BaseController implements UserDataReceiver {
    private static final Logger LOGGER =
            Logger.getLogger(UserAuctionHistoryController.class.getName());

    private static final List<String> STATUS_FILTERS = List.of(
            "All",
            "Winning",
            "Losing",
            "Ended",
            "Cancelled"
    );

    private final Map<String, Parent> cardMap = new HashMap<>();
    private final Map<String, ItemCardController> controllerMap = new HashMap<>();
    private final List<Auction> joinedAuctions = new ArrayList<>();
    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane historyContainer;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;

    // ==================== INIT ====================

    @FXML
    public void initialize() {
        ControllerRegistry.register(UserAuctionHistoryController.class, this);
        System.out.println("AuctionHistory Loaded");
        setupStatusFilter();
        setupCategoryFilter();
        Platform.runLater(() -> {
            historyContainer.setPrefWrapLength(
                    scrollPane.getViewportBounds().getWidth() - 20
            );

            scrollPane.viewportBoundsProperty().addListener(
                    (obs, oldVal, newVal) ->
                            historyContainer.setPrefWrapLength(
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
        loadHistory();
    }

    // ==================== USER ====================
    @Override
    public void setUser(User user) {}

    // ==================== LOAD HISTORY ====================
    public void loadHistory() {
        client.sendMessage(
                "LIST_JOINED_AUCTIONS");
    }

    public void reloadHistory() {
        loadHistory();
    }

    // ==================== FILTER SETUP ====================

    private void setupStatusFilter() {
        statusFilterComboBox.getItems().clear();
        statusFilterComboBox
                .getItems()
                .addAll(STATUS_FILTERS);
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.setOnAction(
                e -> handleFilter()
        );
    }

    private void setupCategoryFilter() {
        categoryFilterComboBox.getItems().clear();
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
                        e -> handleFilter()
                );
    }

    // ==================== RENDER ====================
    public void renderHistory(String response) {
        System.out.println("Auction History Loaded");
        runUI(() -> {
            historyContainer.getChildren().clear();
            joinedAuctions.clear();

            if (response == null
                    || response.equals("LIST_JOINED_AUCTIONS_EMPTY")) {

                showEmptyMessage("No joined auctions");
                return;
            }

            try {

                String rawData =
                        response.substring(
                                "LIST_JOINED_AUCTIONS_SUCCESS|".length()
                        );

                List<Auction> auctions =
                        AuctionParser.parseList(rawData);

                joinedAuctions.addAll(auctions);

                refreshHistory(auctions);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error", e);
                AlertUtils.error(
                        "Failed to load auction history."
                );
            }
        });
    }

    // ==================== FILTER ====================
    private void handleFilter() {
        String selectedStatus =
                statusFilterComboBox.getValue();

        String selectedCategory =
                categoryFilterComboBox.getValue();

        List<Auction> filtered =
                joinedAuctions.stream()

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

        refreshHistory(filtered);
    }

    private boolean matchesStatus(Auction auction, String selectedStatus) {
        if (selectedStatus == null
                || selectedStatus.equalsIgnoreCase(
                "All"
        )) {
            return true;
        }
        return TextUtils.toTitleCase(auction.getStatus().name()).equalsIgnoreCase(selectedStatus);
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
    private void refreshHistory(List<Auction> auctions) {
        historyContainer.getChildren().clear();
        if (auctions.isEmpty()) {
            showEmptyMessage("No matching auctions");
            return;
        }
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
                                card.getProperties()
                                        .get("controller");

                if (controller == null) {
                    continue;
                }

                card.setUserData(id);
                cardMap.put(id, card);
                controllerMap.put(id, controller);
            }
            controller.updateAuction(auction);
            historyContainer.getChildren().add(card);
        }

        cardMap.keySet().removeIf(id -> !newIds.contains(id));
        controllerMap.keySet().removeIf(id -> !newIds.contains(id));
    }

    // ==================== EMPTY ====================
    private void showEmptyMessage(String message) {
        historyContainer
                .getChildren()
                .add(
                        new Label(message)
                );
    }
}


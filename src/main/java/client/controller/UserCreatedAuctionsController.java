package client.controller;

import client.manager.ControllerRegistry;
import client.network.ClientSocket;

import client.util.AuctionCardFactory;
import client.util.TextUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.Parent;

import javafx.scene.control.*;

import javafx.scene.layout.FlowPane;

import model.Auction;

import java.util.*;

public class UserCreatedAuctionsController extends BaseController implements UserDataReceiver{

    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private FlowPane itemGrid;
    @FXML private ScrollPane scrollPane;

    private final List<Auction> CreatedAuctions = new ArrayList<>();
    private final Map<String, Parent> cardMap = new HashMap<>();
    private final Map<String, ItemCardController> controllerMap = new HashMap<>();

    @FXML
    public void initialize() {
        ControllerRegistry.register(UserCreatedAuctionsController.class, this);
        System.out.println("Created Auctions Loaded");

        setupStatusFilter();
        setupCategoryFilter();

        Platform.runLater(() -> {
            itemGrid.setPrefWrapLength(
                    scrollPane.getViewportBounds().getWidth()
            );

            scrollPane.viewportBoundsProperty().addListener(
                    (obs, oldVal, newVal) ->
                            itemGrid.setPrefWrapLength(
                                    newVal.getWidth() - 20
                            )
            );
        });
    }

    @Override
    public void setClient(ClientSocket client) {
        super.setClient(client);

        if (client != null) {
            reloadCreatedAuctions();
        }
    }

    private void setupStatusFilter() {

        statusFilterComboBox.getItems().addAll(
                "All",
                "Active",
                "Ended",
                "Canceled",
                "Upcoming",
                "Pending Approval"
        );

        statusFilterComboBox.setValue(
                "All"
        );

        statusFilterComboBox.setOnAction(
                e -> filterAuctions()
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
                e -> filterAuctions()
        );
    }

    private void filterAuctions() {
        String selectedStatus = statusFilterComboBox.getValue();
        String selectedCategory = categoryFilterComboBox.getValue();
        List<Auction> filtered =
                CreatedAuctions.stream()
                        .filter(auction -> {
                            boolean statusMatch =
                                    selectedStatus == null
                                            || selectedStatus.equalsIgnoreCase("All")
                                            || TextUtils.toTitleCase(
                                            auction.getStatus().name()
                                    ).equalsIgnoreCase(selectedStatus);

                            boolean categoryMatch =
                                    selectedCategory == null
                                            || selectedCategory.equalsIgnoreCase("All")
                                            || TextUtils.toTitleCase(
                                            auction.getItem()
                                                    .getCategory()
                                                    .name()
                                    ).equalsIgnoreCase(selectedCategory);

                            return statusMatch && categoryMatch;
                        })
                        .toList();
        refreshGrid(filtered);
    }

    public void updateCreatedAuctions(List<Auction> auctions) {
        runUI(() -> {
            CreatedAuctions.clear();
            CreatedAuctions.addAll(auctions);

            refreshGrid(CreatedAuctions);
        });
    }

    private void refreshGrid(List<Auction> list) {
        itemGrid.getChildren().clear();
        Set<String> newIds = new HashSet<>();
        for (Auction auction : list) {
            String id = auction.getAuction_id();
            newIds.add(id);
            Parent card = cardMap.get(id);
            ItemCardController controller = controllerMap.get(id);
            if (card == null || controller == null) {
                card =
                        AuctionCardFactory.createCard(
                                auction,
                                ClientSocket.getInstance()
                        );

                if (card == null) {
                    continue;
                }

                controller =
                        (ItemCardController)
                                card.getProperties()
                                        .get("controller");

                if (controller == null) {
                    continue;
                }

                cardMap.put(id, card);
                controllerMap.put(id, controller);
            }
            controller.updateAuction(auction);
            itemGrid.getChildren().add(card);
        }

        cardMap.keySet().removeIf(id -> !newIds.contains(id));
        controllerMap.keySet().removeIf(id -> !newIds.contains(id));
    }

    public void reloadCreatedAuctions() {
        if (client != null) {
            client.sendCreatedAuctions();
        }
    }
}
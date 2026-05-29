package client.controller;

import client.network.ClientSocket;
import client.network.response.parser.AuctionParser;
import client.util.TextUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import model.Auction;
import model.Category;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class AuctionHistoryController
        extends BaseController
        implements UserDataReceiver {

    private static AuctionHistoryController instance;

    public static AuctionHistoryController getInstance() {
        return instance;
    }

    private static final List<String> STATUS_FILTERS = List.of(
            "All",
            "Winning",
            "Losing",
            "Ended",
            "Cancelled"
    );

    private final List<Auction> joinedAuctions =
            new ArrayList<>();

    @FXML private FlowPane historyContainer;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;

    // ==================== INIT ====================

    @FXML
    public void initialize() {
        instance = this;
        System.out.println(
                "AuctionHistory Loaded"
        );
        setupStatusFilter();
        setupCategoryFilter();
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
                    "LIST_JOINED_AUCTIONS_SUCCESS"
            ) || msg.equals(
                    "LIST_JOINED_AUCTIONS_EMPTY"
            )) {

                renderHistory(msg);
            }
        });
        loadHistory();
    }

    // ==================== USER ====================

    @Override
    public void setUser(
            User user
    ) {
    }

    // ==================== LOAD HISTORY ====================

    public void loadHistory() {

        client.sendMessage(
                "LIST_JOINED_AUCTIONS"
        );
    }

    // ==================== FILTER SETUP ====================

    private void setupStatusFilter() {

        statusFilterComboBox
                .getItems()
                .addAll(STATUS_FILTERS);

        statusFilterComboBox
                .setValue("All");

        statusFilterComboBox
                .setOnAction(
                        e -> handleFilter()
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
                        e -> handleFilter()
                );
    }

    // ==================== RENDER ====================

    public void renderHistory(
            String response
    ) {

        runUI(() -> {

            historyContainer
                    .getChildren()
                    .clear();

            joinedAuctions.clear();

            if (response == null
                    || response.equals(
                    "LIST_JOINED_AUCTIONS_EMPTY"
            )) {

                showEmptyMessage(
                        "No joined auctions"
                );

                return;
            }

            try {

                String rawData =
                        response.substring(
                                "LIST_JOINED_AUCTIONS_SUCCESS|"
                                        .length()
                        );

                joinedAuctions.addAll(
                        AuctionParser.parseList(
                                rawData
                        )
                );

                refreshHistory(
                        joinedAuctions
                );

            } catch (Exception e) {

                e.printStackTrace();

                showError(
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

    private void refreshHistory(
            List<Auction> auctions
    ) {

        historyContainer
                .getChildren()
                .clear();

        if (auctions.isEmpty()) {

            showEmptyMessage(
                    "No matching auctions"
            );

            return;
        }

        for (Auction auction : auctions) {

            Parent card =
                    createAuctionCard(
                            auction
                    );

            if (card != null) {

                historyContainer
                        .getChildren()
                        .add(card);
            }
        }
    }

    // ==================== CARD ====================

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

            Parent card =
                    loader.load();

            ItemCardController controller =
                    loader.getController();

            controller.setClient(client);

            controller.setRoot(card);

            controller.setData(auction);

            return card;

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    // ==================== EMPTY ====================

    private void showEmptyMessage(
            String message
    ) {

        historyContainer
                .getChildren()
                .add(
                        new Label(message)
                );
    }
}


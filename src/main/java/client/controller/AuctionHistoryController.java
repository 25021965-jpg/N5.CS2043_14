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
import javafx.scene.layout.HBox;

import model.Auction;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class AuctionHistoryController implements UserDataReceiver {

    private static AuctionHistoryController instance;

    public static AuctionHistoryController getInstance() {
        return instance;
    }

    @FXML private HBox auctionCard;
    @FXML private FlowPane historyContainer;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;

    private final List<Auction> joinedAuctions = new ArrayList<>();

    @FXML
    public void initialize() {
        instance = this;

        System.out.println("AuctionHistory Loaded");

        setupStatusFilter();
        setupCategoryFilter();

        Platform.runLater(this::loadHistory);
    }

    @Override
    public void setClient(ClientSocket client) {
    }

    @Override
    public void setUser(User user) {
    }

    public void loadHistory() {

        ClientSocket socket = ClientSocket.getInstance();

        if (socket == null) {
            return;
        }

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

        statusFilterComboBox.setValue("All");

        statusFilterComboBox.setOnAction(
                e -> handleFilter()
        );
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

        categoryFilterComboBox.setOnAction(
                e -> handleFilter()
        );
    }

    public void renderHistory(String response) {

        Platform.runLater(() -> {

            historyContainer.getChildren().clear();
            joinedAuctions.clear();

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

                joinedAuctions.addAll(
                        AuctionParser.parseList(rawData)
                );

                refreshHistory(joinedAuctions);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleFilter() {

        String selectedStatus =
                statusFilterComboBox.getValue();

        String selectedCategory =
                categoryFilterComboBox.getValue();

        List<Auction> filtered =
                joinedAuctions.stream()

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

                            return statusMatch
                                    && categoryMatch;
                        })

                        .toList();

        refreshHistory(filtered);
    }

    private void refreshHistory(
            List<Auction> auctions
    ) {

        historyContainer.getChildren().clear();

        for (Auction auction : auctions) {

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

                controller.setClient(
                        ClientSocket.getInstance()
                );

                controller.setRoot(card);

                controller.setData(auction);

                historyContainer
                        .getChildren()
                        .add(card);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (historyContainer.getChildren().isEmpty()) {

            historyContainer.getChildren().add(
                    new Label("No matching auctions")
            );
        }
    }
}
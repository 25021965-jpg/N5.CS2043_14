package client.controller;

import client.network.ClientSocket;

import client.util.AuctionCardFactory;
import client.util.TextUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.Parent;

import javafx.scene.control.*;

import javafx.scene.layout.FlowPane;

import model.Auction;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class MyAuctionsController implements UserDataReceiver{

    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private FlowPane itemGrid;
    @FXML private ScrollPane scrollPane;

    private final List<Auction> myAuctions = new ArrayList<>();
    public void setClient(ClientSocket client) {}
    public void setUser(User user) {}
    private static MyAuctionsController instance;
    public static MyAuctionsController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        System.out.println("MyAuctions Loaded");

        setupStatusFilter();
        setupCategoryFilter();

        ClientSocket.getInstance()
                .sendMyAuctions();

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

        String selectedStatus =
                statusFilterComboBox.getValue();

        String selectedCategory =
                categoryFilterComboBox.getValue();

        List<Auction> filtered =
                myAuctions.stream()

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

    public void updateMyAuctions(
            List<Auction> auctions
    ) {

        myAuctions.clear();
        myAuctions.addAll(auctions);

        refreshGrid(myAuctions);
    }

    private void refreshGrid(
            List<Auction> list
    ) {

        itemGrid.getChildren().clear();

        for (Auction auction : list) {

            Parent card =
                    AuctionCardFactory.createCard(
                            auction,
                            ClientSocket.getInstance()
                    );

            if (card != null) {

                itemGrid.getChildren().add(
                        card
                );
            }
        }
    }

    public void reloadMyAuctions() {
        ClientSocket.getInstance().sendMyAuctions();
    }
}
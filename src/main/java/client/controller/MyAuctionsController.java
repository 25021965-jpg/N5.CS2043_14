package client.controller;

import client.network.ClientSocket;

import client.util.TextUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;

import javafx.scene.control.*;

import javafx.scene.layout.GridPane;


import model.Auction;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class MyAuctionsController implements UserDataReceiver{

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> categoryFilterComboBox;

    @FXML
    private GridPane itemGrid;

    private final List<Auction> myAuctions = new ArrayList<>();
    private static final int MAX_COLUMNS = 3;
    public void setClient(ClientSocket client) {
    }

    public void setUser(User user) {
    }
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

        int col = 0;
        int row = 0;

        for (Auction auction : list) {

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

                controller.setData(
                        auction
                );

                itemGrid.add(
                        card,
                        col++,
                        row
                );

                if (col == MAX_COLUMNS) {
                    col = 0;
                    row++;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
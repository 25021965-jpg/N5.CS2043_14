package client.controller;

import client.network.ClientSocket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import javafx.scene.layout.GridPane;

import javafx.stage.Stage;

import model.Auction;
import model.User;

import java.io.IOException;

public class UserViewController {

    private ClientSocket client;

    private User currentUser;

    @FXML
    private GridPane itemGrid;

    @FXML
    private TextField txtSearch;

    private int column = 0;

    private int row = 0;

    @FXML
    public void initialize() {

        System.out.println(
                "UserView Loaded"
        );
    }

    public void setClient(
            ClientSocket client
    ) {

        this.client = client;
    }

    public void setUser(
            User user
    ) {

        this.currentUser = user;

        if (user != null) {

            System.out.println(
                    "Current user: "
                            + user.getUsername()
            );
        }
    }

    @FXML
    private void handleSearch(
            ActionEvent event
    ) {

        System.out.println(
                "Search: "
                        + txtSearch.getText()
        );
    }

    @FXML
    private void handleCreateAuction(
            ActionEvent event
    ) {

        System.out.println(
                "Create Auction clicked"
        );
    }

    @FXML
    private void openProfile(
            ActionEvent event
    ) {

        openProfilePage(event);
    }

    @FXML
    private void openHistory(
            ActionEvent event
    ) {

        System.out.println(
                "Open History"
        );
    }

    @FXML
    private void openYourAuctions(
            ActionEvent event
    ) {

        System.out.println(
                "Open Your Auctions"
        );
    }

    @FXML
    private void openFavourite(
            ActionEvent event
    ) {

        System.out.println(
                "Open Favourite"
        );
    }

    @FXML
    private void openBalance(
            ActionEvent event
    ) {

        System.out.println(
                "Open Balance"
        );
    }

    public void addNewAuctionCard(
            Auction auction
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/itemsCard-view.fxml"
                            )
                    );

            Node card =
                    loader.load();

            ItemCardController controller =
                    loader.getController();

            controller.setData(auction);

            if (column == 3) {

                column = 0;

                row++;
            }

            itemGrid.add(
                    card,
                    column++,
                    row
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @FXML
    private void openProfileDirect(
            ActionEvent event
    ) {

        openProfilePage(event);
    }

    private void openProfilePage(
            ActionEvent event
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/userProfile-view.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            ProfileController controller =
                    loader.getController();

            controller.setClient(client);

            controller.setUser(currentUser);

            Stage stage;

            if (
                    event.getSource()
                            instanceof MenuItem
            ) {

                MenuItem item =
                        (MenuItem)
                                event.getSource();

                stage =
                        (Stage)
                                item.getParentPopup()
                                        .getOwnerWindow();

            } else {

                stage =
                        (Stage)
                                ((Node)
                                        event.getSource())
                                        .getScene()
                                        .getWindow();
            }

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Profile"
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
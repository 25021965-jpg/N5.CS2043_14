package client.controller;

import client.network.ClientSocket;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.GridPane;

import javafx.stage.Stage;

import model.Auction;
import model.Category;
import model.User;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HomePageController implements UserDataReceiver {

    private static final int MAX_COLUMNS = 3;

    private ClientSocket client;

    private User currentUser;

    private final List<Auction> auctionList =
            new ArrayList<>();

    private int column = 0;

    private int row = 0;

    @FXML
    private GridPane itemGrid;

    @FXML
    private TextField txtSearch;

    @FXML
    public void initialize() {

        log(
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

            log(
                    "Current user: "
                            + user.getUsername()
            );
        }
    }

    @FXML
    private void handleSearch(
            ActionEvent event
    ) {

        String keyword =
                txtSearch.getText()
                        .trim()
                        .toLowerCase();

        log(
                "Search: "
                        + keyword
        );

        if (keyword.isEmpty()) {

            refreshGrid(
                    auctionList
            );

            return;
        }

        List<Auction> filtered =
                new ArrayList<>();

        for (Auction auction : auctionList) {

            String itemName =
                    auction.getItem()
                            .getName()
                            .toLowerCase();

            if (
                    itemName.contains(
                            keyword
                    )
            ) {

                filtered.add(
                        auction
                );
            }
        }

        refreshGrid(
                filtered
        );
    }

    @FXML
    private void showAccessories() {

        filterByCategory(
                Category.ACCESSORIES
        );
    }

    @FXML
    private void showCollectibles() {

        filterByCategory(
                Category.COLLECTIBLES
        );
    }

    @FXML
    private void showElectronics() {

        filterByCategory(
                Category.ELECTRONICS
        );
    }

    @FXML
    private void showFashion() {

        filterByCategory(
                Category.FASHION
        );
    }

    @FXML
    private void showHomeAppliances() {

        filterByCategory(
                Category.HOME_APPLIANCES
        );
    }

    @FXML
    private void showVehicles() {

        filterByCategory(
                Category.VEHICLES
        );
    }

    @FXML
    private void showOther() {

        filterByCategory(
                Category.OTHER
        );
    }

    @FXML
    private void showAll() {

        refreshGrid(
                auctionList
        );
    }

    private void filterByCategory(
            Category category
    ) {

        List<Auction> filtered =
                new ArrayList<>();

        for (Auction auction : auctionList) {

            if (
                    auction.getItem()
                            .getCategory()
                            == category
            ) {

                filtered.add(
                        auction
                );
            }
        }

        refreshGrid(
                filtered
        );
    }

    public void addNewAuctionCard(
            Auction auction
    ) {

        auctionList.add(
                auction
        );

        addAuctionCard(
                auction
        );
    }

    private void refreshGrid(
            List<Auction> list
    ) {

        itemGrid.getChildren().clear();

        column = 0;
        row = 0;

        for (Auction auction : list) {

            addAuctionCard(
                    auction
            );
        }
    }

    private void addAuctionCard(
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

            controller.setData(
                    auction
            );

            if (
                    column == MAX_COLUMNS
            ) {

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
    private void handleCreateAuction(
            ActionEvent event
    ) {

        openPage(
                "/fxml/createAuction-view.fxml",
                "Create Auction",
                event
        );
    }

    @FXML
    private void openProfile(
            ActionEvent event
    ) {

        openPage(
                "/fxml/userProfile-view.fxml",
                "Profile",
                event
        );
    }

    @FXML
    private void openProfileDirect(
            ActionEvent event
    ) {

        openProfile(
                event
        );
    }

    @FXML
    private void openHistory(
            ActionEvent event
    ) {

        // TODO:
        // open history page

        /*
        openPage(
                "/fxml/auctionHistory-view.fxml",
                "History",
                event
        );
        */
    }

    @FXML
    private void openYourAuctions(
            ActionEvent event
    ) {

        openPage(
                "/fxml/yourAuctions-view.fxml",
                "Your Auctions",
                event
        );
    }

    @FXML
    private void openFavourite(
            ActionEvent event
    ) {
        openPage(
                "/fxml/Favourite-view.fxml",
                "Favourite",
                event
        );
    }

    @FXML
    private void openBalance(
            ActionEvent event
    ) {

        // TODO:
        // open balance page

        /*
        openPage(
                "/fxml/balance-view.fxml",
                "Balance",
                event
        );
        */
    }

    @FXML
    private void handleLogout(
            ActionEvent event
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                "Logout"
        );

        alert.setHeaderText(
                "Are you sure you want to logout?"
        );

        alert.setContentText(
                "You will need to login again."
        );

        ButtonType logoutButton =
                new ButtonType(
                        "Logout"
                );

        ButtonType cancelButton =
                new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        alert.getButtonTypes().setAll(
                logoutButton,
                cancelButton
        );

        Optional<ButtonType> result =
                alert.showAndWait();

        if (
                result.isPresent()
                        &&
                        result.get() == logoutButton
        ) {

            log(
                    "Logout clicked"
            );

            openPage(
                    "/fxml/login-view.fxml",
                    "Login",
                    event
            );
        }
    }

    @FXML
    private void handleDeleteAccount(
            ActionEvent event
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                "Delete Account"
        );

        alert.setHeaderText(
                "Are you sure you want to delete your account?"
        );

        alert.setContentText(
                "This action cannot be undone."
        );

        ButtonType deleteButton =
                new ButtonType(
                        "Delete"
                );

        ButtonType cancelButton =
                new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        alert.getButtonTypes().setAll(
                deleteButton,
                cancelButton
        );

        Optional<ButtonType> result =
                alert.showAndWait();

        if (
                result.isPresent()
                        &&
                        result.get() == deleteButton
        ) {

            log(
                    "Account deleted"
            );

            openPage(
                    "/fxml/login-view.fxml",
                    "Login",
                    event
            );
        }
    }

    private void openPage(
            String fxmlPath,
            String title,
            ActionEvent event
    ) {

        try {

            log(
                    "Opening: "
                            + fxmlPath
            );

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    fxmlPath
                            )
                    );

            Parent root =
                    loader.load();

            Object controller =
                    loader.getController();

            passData(
                    controller
            );

            Stage stage =
                    getStage(
                            event
                    );

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    title
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            log(
                    "Cannot open: "
                            + fxmlPath
            );
        }
    }

    private void passData(Object controller) {

        if (controller instanceof UserDataReceiver c) {

            c.setClient(client);
            c.setUser(currentUser);
        }


        if (
                controller instanceof FavouriteController c
        ) {

            c.setClient(client);
            c.setUser(currentUser);
        }

        /*
        if (
                controller instanceof BalanceController c
        ) {

            c.setClient(client);
            c.setUser(currentUser);
        }

        if (
                controller instanceof AuctionHistoryController c
        ) {

            c.setClient(client);
            c.setUser(currentUser);
        }
        */
    }

    private Stage getStage(
            ActionEvent event
    ) {

        if (
                event.getSource()
                        instanceof MenuItem
        ) {

            MenuItem item =
                    (MenuItem)
                            event.getSource();

            return
                    (Stage)
                            item.getParentPopup()
                                    .getOwnerWindow();
        }

        return
                (Stage)
                        ((Node)
                                event.getSource())
                                .getScene()
                                .getWindow();
    }

    private void log(
            String message
    ) {

        System.out.println(
                "[UserView] "
                        + message
        );
    }
}
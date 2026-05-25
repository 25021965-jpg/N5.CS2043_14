package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.GridPane;

import javafx.stage.Stage;

import model.Auction;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MyAuctionsController implements UserDataReceiver{

    private ClientSocket client;

    private User currentUser;

    @FXML
    private Button infoBtn;

    @FXML
    private Button historyBtn;

    @FXML
    private Button createdAuctionBtn;

    @FXML
    private Button favoriteBtn;

    @FXML
    private Button balanceBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Button deleteAccountBtn;

    @FXML
    private Button backHomeBtn;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> categoryFilterComboBox;

    @FXML
    private GridPane itemGrid;

    private static MyAuctionsController instance;
    public static MyAuctionsController getInstance() {
        return instance;
    }

    private final List<Auction> myAuctions = new ArrayList<>();
    private static final int MAX_COLUMNS = 3;
    public void setClient(ClientSocket client) {
        this.client = client;
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        instance = this;
        System.out.println("MyAuctions Loaded");

        setupStatusFilter();
        setupCategoryFilter();
        setupMenuEvents();

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

    private void setupMenuEvents() {

        infoBtn.setOnAction(
                e -> openPage(
                        "/fxml/userProfile-view.fxml",
                        "Profile"
                )
        );

        createdAuctionBtn.setOnAction(
                e -> openPage(
                        "/fxml/myAuctions-view.fxml",
                        "My Auctions"
                )
        );

        backHomeBtn.setOnAction(
                e -> openPage(
                        "/fxml/HomePage.fxml",
                        "Home"
                )
        );

        logoutBtn.setOnAction(
                e -> handleLogout()
        );

        deleteAccountBtn.setOnAction(
                e -> handleDeleteAccount()
        );

        historyBtn.setOnAction(
                e ->
                        openPage("/fxml/auctionHistory-view.fxml",
                                "Profile"
                        )
        );

        favoriteBtn.setOnAction(
                e -> openPage(
                        "/fxml/favourite-view.fxml",
                        "Favourite"
                )
        );

        balanceBtn.setOnAction(
                e -> openPage(
                        "/fxml/accountBalance-view.fxml",
                        "Account Balance"
                )
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
                                            || auction.getStatus()
                                            .name()
                                            .replace("_", " ")
                                            .equalsIgnoreCase(selectedStatus);

                            boolean categoryMatch =
                                    selectedCategory == null
                                            || selectedCategory.equalsIgnoreCase("All")
                                            || auction.getItem()
                                            .getCategory()
                                            .name()
                                            .replace("_", " ")
                                            .equalsIgnoreCase(selectedCategory);

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

    @FXML
    private void handleLogout() {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                "Logout"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                "Are you sure you want to logout?"
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

        if (result.isPresent()
                && result.get() == logoutButton) {

            client.logout();

            UserSession.setCurrentUser(null);

            openPage(
                    "/fxml/login-view.fxml",
                    "Login"
            );
        }
    }

    @FXML
    private void handleDeleteAccount() {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Delete Account"
        );

        alert.setHeaderText(null);
        alert.setContentText(
                "Are you sure you want to delete your account? This action cannot be undone."
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

            System.out.println(
                    "Account deleted"
            );

        /*
            TODO:
            - delete account from server
            - clear session
         */

            openPage(
                    "/fxml/login-view.fxml",
                    "Login"
            );
        }
    }

    private void openPage(
            String fxmlPath,
            String title
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    fxmlPath
                            )
                    );

            Parent root =
                    loader.load();

            passData(
                    loader.getController()
            );

            Stage stage =
                    (Stage)
                            backHomeBtn
                                    .getScene()
                                    .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    title
            );

            stage.show();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.out.println("Cannot open: " + fxmlPath);
        }
    }

    private void passData(Object controller) {

        if (controller instanceof UserDataReceiver c) {

            c.setClient(client);
            c.setUser(currentUser);
        }
    }
}
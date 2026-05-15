package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.HBox;

import javafx.stage.Stage;

import model.User;

import java.io.IOException;
import java.util.Optional;

public class YourAuctionsController implements UserDataReceiver{

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
    private HBox auctionCard;

    @FXML
    private Button cancelAuctionBtn;

    public void setClient(
            ClientSocket client
    ) {

        this.client = client;
    }

    public void setUser(
            User user
    ) {

        this.currentUser = user;
    }

    @FXML
    public void initialize() {

        System.out.println(
                "YourAuctions Loaded"
        );

        setupStatusFilter();
        setupCategoryFilter();
        setupMenuEvents();
        setupCardEvents();
    }

    private void setupStatusFilter() {

        statusFilterComboBox.getItems().addAll(
                "All",
                "Active",
                "Finished",
                "Canceled"
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

    private void setupCardEvents() {

        auctionCard.setOnMouseClicked(e -> {

            System.out.println(
                    "Opening auction detail..."
            );

            openPage(
                    "/fxml/items-view.fxml",
                    "Auction Detail"
            );
        });

        auctionCard.setOnMouseEntered(e -> auctionCard.setStyle(
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #D4AF37;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 15;" +
                        "-fx-cursor: hand;"
        ));

        auctionCard.setOnMouseExited(e -> auctionCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #D4AF37;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 15;" +
                        "-fx-cursor: hand;"
        ));

        cancelAuctionBtn.setOnMouseClicked(
                Event::consume
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
                        "/fxml/yourAuctions-view.fxml",
                        "Your Auctions"
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

    @FXML
    private void handleCancelAuction() {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Cancel Auction"
        );

        alert.setHeaderText(null);
        alert.setContentText(
                "Are you sure you want to cancel this auction? Users will no longer be able to bid on this item."
        );

        ButtonType cancelAuctionButton =
                new ButtonType(
                        "Cancel Auction"
                );

        ButtonType closeButton =
                new ButtonType(
                        "Close",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        alert.getButtonTypes().setAll(
                cancelAuctionButton,
                closeButton
        );

        Optional<ButtonType> result =
                alert.showAndWait();

        if (
                result.isPresent()
                        &&
                        result.get()
                                == cancelAuctionButton
        ) {

            System.out.println(
                    "Auction cancelled!"
            );

        /*
            TODO:
            - update auction status
            - send request to server
            - refresh UI
         */
        }
    }

    private void filterAuctions() {

        String selectedStatus =
                statusFilterComboBox.getValue();
        String selectedCategory =
                categoryFilterComboBox.getValue();

        System.out.println(
                "Filter: "
                        + selectedStatus
        );
        System.out.println(
                "Category: "
                        + selectedCategory
        );

        /*
            TODO:
            - filter auction by status
            - update UI
         */
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
package client.controller;

import client.manager.UserSession;
import client.network.ClientSocket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import model.User;

import java.io.IOException;
import java.util.Optional;

public class AuctionHistoryController implements UserDataReceiver {

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
    private HBox auctionCard;

    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private ComboBox<String> categoryFilterComboBox;

    @FXML
    private VBox historyContainer;

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
                "AuctionHistory Loaded"
        );

        setupStatusFilter();
        setupCategoryFilter();
        setupMenuEvents();
        setupCardEvents();
    }

    private void setupCardEvents() {

        auctionCard.setOnMouseClicked(e -> {

            System.out.println("Opening auction details...");

            openPage(
                    "/fxml/items-view.fxml",
                    "Auction Detail"
            );
        });

        auctionCard.setOnMouseEntered(e -> {

            auctionCard.setStyle(
                    "-fx-background-color: #F8FAFC;" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-color: #D4AF37;" +
                            "-fx-border-width: 2;" +
                            "-fx-padding: 15;" +
                            "-fx-cursor: hand;"
            );
        });

        auctionCard.setOnMouseExited(e -> {

            auctionCard.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-color: #D4AF37;" +
                            "-fx-border-width: 2;" +
                            "-fx-padding: 15;" +
                            "-fx-cursor: hand;"
            );
        });
    }


    private void setupStatusFilter() {

        statusFilterComboBox.getItems().addAll(
                "All",
                "Winning",
                "Losing",
                "Ended",
                "Cancelled"
        );

        statusFilterComboBox.setValue(
                "All"
        );

        statusFilterComboBox.setOnAction(
                e -> handleFilter()
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
                e -> handleFilter()
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
                e -> openPage(
                        "/fxml/auctionHistory-view.fxml",
                        "Auction History"
                )
        );

        favoriteBtn.setOnAction(
                e -> openPage(
                        "/fxml/Favourite-view.fxml",
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

    private void handleFilter() {

        String selected =
                statusFilterComboBox.getValue();
        String selectedCategory =
                categoryFilterComboBox.getValue();

        System.out.println(
                "Status: "
                        + selected
        );
        System.out.println(
                "Category: "
                        + selectedCategory
        );

        /*
            TODO:
            - filter history by status
         */
    }

    @FXML
    private void handleLogout() {

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText(
                "Are you sure you want to logout?"
        );

        ButtonType logoutButton =
                new ButtonType("Logout");

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

            ClientSocket socket =
                    ClientSocket.getInstance();

            if (socket != null) {
                socket.logout();
            }

            UserSession.setCurrentUser(null);

            openPage(
                    "/fxml/login-view.fxml",
                    "Login"
            );
        }
    }

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